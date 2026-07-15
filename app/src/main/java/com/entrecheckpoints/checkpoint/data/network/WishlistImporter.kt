package com.entrecheckpoints.checkpoint.data.network

class WishlistImporter(
    private val http: HttpClient,
    private val stores: StoreRegistry,
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
                isSteamWishlist(url) -> {
                    sources += 1
                    output += extractSteamWishlist(url)
                }
                stores.detect(url) != null -> output += url
                else -> ignored += 1
            }
        }

        raw.lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .filterNot { URL_REGEX.containsMatchIn(it) }
            .forEach { line ->
                val appId = Regex("""^(?:steam:|appid\s*[:#]?\s*)?(\d{3,10})$""", RegexOption.IGNORE_CASE)
                    .matchEntire(line)?.groupValues?.getOrNull(1)
                if (appId != null) output += "https://store.steampowered.com/app/$appId/?cc=mx&l=spanish"
                else ignored += 1
            }

        if (output.isEmpty()) {
            throw StoreFetchException("No encontré enlaces de productos compatibles ni una wishlist pública legible.")
        }
        return Result(output.toList(), ignored, sources)
    }

    private suspend fun extractSteamWishlist(url: String): List<String> {
        val response = http.get(url)
        val ids = linkedSetOf<String>()
        Regex("""store\.steampowered\.com/app/(\d+)""", RegexOption.IGNORE_CASE)
            .findAll(response.body)
            .forEach { ids += it.groupValues[1] }
        Regex("""[\"']appid[\"']\s*:\s*[\"']?(\d+)[\"']?""", RegexOption.IGNORE_CASE)
            .findAll(response.body)
            .forEach { ids += it.groupValues[1] }
        Regex("""data-app-id=[\"'](\d+)[\"']""", RegexOption.IGNORE_CASE)
            .findAll(response.body)
            .forEach { ids += it.groupValues[1] }
        if (ids.isEmpty()) {
            throw StoreFetchException("La wishlist de Steam no es pública o Steam no expuso sus App IDs en la página.")
        }
        return ids.map { "https://store.steampowered.com/app/$it/?cc=mx&l=spanish" }
    }

    private fun isSteamWishlist(url: String): Boolean =
        url.contains("store.steampowered.com/wishlist", ignoreCase = true)

    companion object {
        private val URL_REGEX = Regex("""https?://[^\s<>\"']+""", RegexOption.IGNORE_CASE)
    }
}
