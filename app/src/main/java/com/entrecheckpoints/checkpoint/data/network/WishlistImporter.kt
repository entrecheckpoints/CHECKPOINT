package com.entrecheckpoints.checkpoint.data.network

import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class WishlistImporter(
    private val http: HttpClient,
    private val stores: StoreRegistry,
    private val nintendoResolver: NintendoSharedWishlistResolver? = null,
) {
    data class Result(
        val productUrls: List<String>,
        val ignoredLines: Int,
        val sourceCount: Int,
    )

    suspend fun resolve(raw: String): Result {
        val direct = URL_REGEX.findAll(raw)
            .map { it.value.trimEnd('.', ',', ';', ')', ']', '}') }
            .distinct()
            .toList()
        val output = linkedSetOf<String>()
        var ignored = 0
        var sources = 0

        direct.forEach { url ->
            when {
                isSteamWishlistSource(url) -> {
                    sources += 1
                    output += extractSteamWishlist(url)
                }
                isNintendoSharedWishlistSource(url) -> {
                    val shared = parseNintendoSharedWishlist(url)
                        ?: throw StoreFetchException("El enlace compartido de Nintendo no contiene SKU válidos.")
                    val resolver = nintendoResolver
                        ?: throw StoreFetchException("El lector visual de wishlists de Nintendo no está disponible.")
                    sources += 1
                    val resolved = resolver.resolve(shared.url, shared.skus.size)
                    output += resolved
                    ignored += (shared.skus.size - resolved.size).coerceAtLeast(0)
                }
                isUnsupportedWishlist(url) -> ignored += 1
                stores.detect(url) != null -> output += url
                else -> ignored += 1
            }
        }

        // También acepta una respuesta JSON copiada desde wishlistdata o cualquier texto
        // que contenga campos appid claramente identificables.
        if (raw.trimStart().startsWith('{') || raw.trimStart().startsWith('[')) {
            extractSteamAppIds(raw).forEach { appId -> output += steamProductUrl(appId) }
        }

        raw.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .filterNot { URL_REGEX.containsMatchIn(it) }
            .forEach { line ->
                val appId = APP_ID_LINE_REGEX.matchEntire(line)?.groupValues?.getOrNull(1)
                if (appId != null) output += steamProductUrl(appId)
                else if (!(line.startsWith('{') || line.startsWith('[') || line.startsWith('}') || line.startsWith(']'))) {
                    ignored += 1
                }
            }

        if (output.isEmpty()) {
            throw StoreFetchException(
                "No encontré productos compatibles. Checkpoint admite wishlists públicas de Steam, " +
                    "wishlists compartidas de Nintendo y enlaces individuales de Nintendo, Steam o Xbox.",
            )
        }
        return Result(output.toList(), ignored, sources)
    }

    private suspend fun extractSteamWishlist(inputUrl: String): List<String> {
        val bases = linkedSetOf<String>()
        bases += steamWishlistBases(inputUrl)

        // Obtener la página una vez permite seguir redirecciones de vanity URL a SteamID
        // y también sirve como respaldo cuando Steam incrusta parte de los datos en HTML.
        val landingIds = linkedSetOf<String>()
        val initialBases = bases.toList()
        initialBases.forEach { base ->
            runCatching {
                http.get(
                    base,
                    headers = mapOf(
                        "Accept" to "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8",
                        "Referer" to "https://store.steampowered.com/",
                    ),
                )
            }.onSuccess { response ->
                landingIds += extractSteamAppIds(response.body)
                bases += steamWishlistBases(response.finalUrl)
            }
        }

        for (base in bases) {
            val ids = linkedSetOf<String>()
            ids += landingIds

            for (page in 0 until MAX_WISHLIST_PAGES) {
                val endpoint = "${base.trimEnd('/')}/wishlistdata/?p=$page&cc=mx&l=spanish"
                val response = runCatching {
                    http.get(
                        endpoint,
                        headers = mapOf(
                            "Accept" to "application/json,text/javascript,*/*;q=0.8",
                            "X-Requested-With" to "XMLHttpRequest",
                            "Referer" to "${base.trimEnd('/')}/",
                        ),
                    )
                }.getOrNull() ?: break

                val pageIds = extractSteamAppIds(response.body)
                if (pageIds.isEmpty()) break

                val sizeBefore = ids.size
                ids += pageIds
                if (ids.size == sizeBefore || ids.size >= MAX_WISHLIST_ITEMS) break
            }

            if (ids.isNotEmpty()) {
                return ids.take(MAX_WISHLIST_ITEMS).map(::steamProductUrl)
            }
        }

        throw StoreFetchException(
            "No pude leer esa wishlist de Steam. Confirma que sea pública y comparte una URL que contenga " +
                "/wishlist/profiles/<SteamID>/ o /wishlist/id/<usuario>/.",
        )
    }

    private fun isSteamWishlistSource(url: String): Boolean {
        val normalized = url.lowercase()
        return normalized.contains("store.steampowered.com/wishlist/") ||
            normalized.matches(Regex("https?://(?:www\\.)?steamcommunity\\.com/(?:profiles/\\d+|id/[^/?#]+).*"))
    }

    private fun isNintendoSharedWishlistSource(url: String): Boolean = runCatching {
        val uri = URI(url.trim())
        uri.host?.endsWith("nintendo.com", ignoreCase = true) == true &&
            uri.path.orEmpty().contains("/wish-list/share/", ignoreCase = true)
    }.getOrDefault(false)

    private fun isUnsupportedWishlist(url: String): Boolean {
        val normalized = url.lowercase()
        return ((normalized.contains("xbox.com") || normalized.contains("microsoft.com")) &&
            normalized.contains("wishlist")) ||
            (normalized.contains("nintendo.") && normalized.contains("wish-list") &&
                !normalized.contains("/wish-list/share/"))
    }

    companion object {
        private const val MAX_WISHLIST_PAGES = 100
        private const val MAX_WISHLIST_ITEMS = 2_000
        private const val MAX_NINTENDO_WISHLIST_ITEMS = 500

        private val URL_REGEX = Regex("""https?://[^\s<>\"']+""", RegexOption.IGNORE_CASE)
        private val APP_ID_LINE_REGEX = Regex(
            """^(?:steam:|appid\s*[:#]?\s*)?(\d{3,10})$""",
            RegexOption.IGNORE_CASE,
        )


        internal data class NintendoSharedWishlist(
            val url: String,
            val locale: String,
            val region: String,
            val skus: List<String>,
            val createdAtMillis: Long?,
        )

        internal fun parseNintendoSharedWishlist(inputUrl: String): NintendoSharedWishlist? {
            val uri = runCatching { URI(inputUrl.trim()) }.getOrNull() ?: return null
            val host = uri.host?.lowercase() ?: return null
            if (!host.endsWith("nintendo.com")) return null

            val path = uri.path.orEmpty()
            val locale = Regex("""/([a-z]{2}-[a-z]{2})/wish-list/share/?""", RegexOption.IGNORE_CASE)
                .find(path)
                ?.groupValues
                ?.getOrNull(1)
                ?.lowercase()
                ?: return null

            val parameters = uri.rawFragment.orEmpty()
                .split('&')
                .mapNotNull { entry ->
                    val pieces = entry.split('=', limit = 2)
                    if (pieces.size != 2) null
                    else pieces[0].lowercase() to URLDecoder.decode(pieces[1], StandardCharsets.UTF_8.name())
                }
                .toMap()

            val skus = parameters["skus"]
                .orEmpty()
                .split(',')
                .map(String::trim)
                .filter { it.matches(Regex("\\d{8,16}")) }
                .distinct()
                .take(MAX_NINTENDO_WISHLIST_ITEMS)

            if (skus.isEmpty()) return null
            val region = locale.substringAfter('-', "mx").uppercase()
            val createdAt = parameters["date"]?.toLongOrNull()

            return NintendoSharedWishlist(
                url = inputUrl.trim(),
                locale = locale,
                region = region,
                skus = skus,
                createdAtMillis = createdAt,
            )
        }

        internal fun steamWishlistBases(inputUrl: String): List<String> {
            val clean = inputUrl.substringBefore('#').substringBefore('?').trimEnd('/')
            val uri = runCatching { URI(clean) }.getOrNull() ?: return emptyList()
            val host = uri.host?.lowercase() ?: return emptyList()
            val segments = uri.path.orEmpty().split('/').filter(String::isNotBlank)
            val output = linkedSetOf<String>()

            when {
                host.endsWith("store.steampowered.com") -> {
                    val wishlistIndex = segments.indexOfFirst { it.equals("wishlist", ignoreCase = true) }
                    if (wishlistIndex >= 0 && segments.size >= wishlistIndex + 3) {
                        val kind = segments[wishlistIndex + 1].lowercase()
                        val identity = segments[wishlistIndex + 2]
                        if (kind == "profiles" || kind == "id") {
                            output += "https://store.steampowered.com/wishlist/$kind/$identity"
                        }
                    }
                }
                host.endsWith("steamcommunity.com") && segments.size >= 2 -> {
                    val kind = segments[0].lowercase()
                    val identity = segments[1]
                    if (kind == "profiles" || kind == "id") {
                        output += "https://store.steampowered.com/wishlist/$kind/$identity"
                    }
                }
            }
            return output.toList()
        }

        internal fun extractSteamAppIds(payload: String): Set<String> {
            val ids = linkedSetOf<String>()
            val trimmed = payload.trim()

            if (trimmed.startsWith('{')) {
                runCatching { collectJsonIds(JSONObject(trimmed), ids) }
            } else if (trimmed.startsWith('[')) {
                runCatching { collectJsonIds(JSONArray(trimmed), ids) }
            }

            Regex("""store\.steampowered\.com/app/(\d+)""", RegexOption.IGNORE_CASE)
                .findAll(payload)
                .forEach { ids += it.groupValues[1] }
            Regex("""[\"']appid[\"']\s*:\s*[\"']?(\d+)[\"']?""", RegexOption.IGNORE_CASE)
                .findAll(payload)
                .forEach { ids += it.groupValues[1] }
            Regex("""data-app-id=[\"'](\d+)[\"']""", RegexOption.IGNORE_CASE)
                .findAll(payload)
                .forEach { ids += it.groupValues[1] }
            Regex("""[\"'](\d{3,10})[\"']\s*:\s*\{""", RegexOption.IGNORE_CASE)
                .findAll(payload)
                .forEach { ids += it.groupValues[1] }

            return ids.filterTo(linkedSetOf()) { it.length in 3..10 }
        }

        private fun collectJsonIds(value: Any?, ids: MutableSet<String>) {
            when (value) {
                is JSONObject -> {
                    val keys = value.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val child = value.opt(key)
                        if (key.matches(Regex("\\d{3,10}")) && child is JSONObject) ids += key
                        if (key.equals("appid", ignoreCase = true)) {
                            value.optString(key).takeIf { it.matches(Regex("\\d{3,10}")) }?.let(ids::add)
                        }
                        collectJsonIds(child, ids)
                    }
                }
                is JSONArray -> {
                    for (index in 0 until value.length()) collectJsonIds(value.opt(index), ids)
                }
            }
        }

        private fun steamProductUrl(appId: String): String =
            "https://store.steampowered.com/app/$appId/?cc=mx&l=spanish"
    }
}
