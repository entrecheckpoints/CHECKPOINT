package com.entrecheckpoints.checkpoint.data.network

import android.net.Uri
import com.entrecheckpoints.checkpoint.data.model.ProductSnapshot
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.data.model.SubscriptionService
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SteamParser(private val http: HttpClient) : StoreParser {
    override val store: Store = Store.STEAM

    data class SteamProduct(val type: String, val id: String)

    override fun matches(url: String): Boolean = extractProduct(url) != null

    fun extractProduct(url: String): SteamProduct? {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return null
        if (uri.host?.endsWith("steampowered.com", ignoreCase = true) != true) return null
        return Regex("""/(app|sub|bundle)/(\d+)""", RegexOption.IGNORE_CASE)
            .find(uri.path.orEmpty())
            ?.let { SteamProduct(it.groupValues[1].lowercase(), it.groupValues[2]) }
    }

    override suspend fun fetch(url: String): ProductSnapshot {
        val product = extractProduct(url) ?: throw StoreFetchException("El enlace de Steam no contiene un App ID válido.")
        val region = ParserUtils.regionFromUrl(url, "MX")
        if (product.type == "app") {
            val endpoint = "https://store.steampowered.com/api/appdetails?appids=${product.id}&cc=${region.lowercase()}&l=spanish"
            val apiSnapshot = runCatching { parseApi(http.get(endpoint).body, product, region) }.getOrNull()
            if (apiSnapshot != null) {
                if (apiSnapshot.discountPercent > 0) {
                    val enriched = runCatching {
                        val page = http.get(
                            normalizeUrl(product, region),
                            mapOf("Cookie" to "birthtime=568022401; lastagecheckage=1-January-1988"),
                        )
                        parseHtml(page.body, page.finalUrl, product, region)
                    }.getOrNull()
                    return apiSnapshot.copy(
                        offerEndsAt = enriched?.offerEndsAt,
                        detectedSubscriptions = enriched?.detectedSubscriptions.orEmpty(),
                    )
                }
                return apiSnapshot
            }
        }
        val normalized = normalizeUrl(product, region)
        val response = http.get(normalized, mapOf("Cookie" to "birthtime=568022401; lastagecheckage=1-January-1988"))
        return parseHtml(response.body, response.finalUrl, product, region)
            ?: throw StoreFetchException("Steam no devolvió un precio legible para este producto.")
    }

    fun parseApi(json: String, product: SteamProduct, region: String): ProductSnapshot? {
        val root = JSONObject(json).optJSONObject(product.id) ?: return null
        if (!root.optBoolean("success", false)) return null
        val data = root.optJSONObject("data") ?: return null
        val isFree = data.optBoolean("is_free", false)
        val price = data.optJSONObject("price_overview")
        val finalCents = when {
            isFree -> 0L
            price != null && price.has("final") -> price.optLong("final")
            else -> return null
        }
        val initialCents = if (isFree) 0L else price?.optLong("initial", finalCents) ?: finalCents
        val currency = price?.optString("currency")?.takeIf(String::isNotBlank)
            ?: ParserUtils.defaultCurrency(region)
        return ProductSnapshot(
            store = store,
            productId = product.id,
            productType = product.type,
            title = data.optString("name", "Juego de Steam"),
            url = normalizeUrl(product, region),
            imageUrl = data.optString("header_image").takeIf(String::isNotBlank),
            priceCents = finalCents,
            regularPriceCents = initialCents,
            currency = currency,
            region = region,
            discountPercent = price?.optInt("discount_percent", ParserUtils.discountPercent(initialCents, finalCents))
                ?: ParserUtils.discountPercent(initialCents, finalCents),
            source = "steam-api",
        )
    }

    fun parseHtml(html: String, pageUrl: String, product: SteamProduct, region: String): ProductSnapshot? {
        val free = Regex("""\bFree to Play\b|\bGratis\b""", RegexOption.IGNORE_CASE).containsMatchIn(html)
        val finalRaw = Regex("""data-price-final=[\"'](\d+)[\"']""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)?.toLongOrNull()
        val metaPrice = ParserUtils.meta(html, "price") ?: ParserUtils.meta(html, "product:price:amount")
        val priceCents = when {
            free -> 0L
            finalRaw != null -> finalRaw
            else -> ParserUtils.parseMoneyToCents(metaPrice)
        } ?: Regex("""game_purchase_price[^>]*>([^<]+)""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)?.let(ParserUtils::parseMoneyToCents)
            ?: return null

        val originalCents = Regex("""discount_original_price[^>]*>([^<]+)""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)?.let(ParserUtils::parseMoneyToCents)
            ?: priceCents
        val discount = Regex("""discount_pct[^>]*>\s*-?(\d+)%""", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: ParserUtils.discountPercent(originalCents, priceCents)

        return ProductSnapshot(
            store = store,
            productId = product.id,
            productType = product.type,
            title = cleanTitle(ParserUtils.titleFromHtml(html, "Juego de Steam")),
            url = normalizeUrl(product, region),
            imageUrl = ParserUtils.imageFromHtml(html),
            priceCents = priceCents,
            regularPriceCents = originalCents,
            currency = ParserUtils.meta(html, "priceCurrency") ?: ParserUtils.defaultCurrency(region),
            region = region,
            discountPercent = discount,
            offerEndsAt = extractOfferEnd(html),
            detectedSubscriptions = buildSet {
                if (Regex("""\b(Included with|Incluido con) EA Play\b""", RegexOption.IGNORE_CASE).containsMatchIn(html)) add(SubscriptionService.EA_PLAY)
            },
            source = "steam-html",
        )
    }

    private fun extractOfferEnd(html: String): Long? {
        val raw = listOf(
            Regex("""data-discount-expiration=[\"'](\d{10,13})[\"']""", RegexOption.IGNORE_CASE),
            Regex("""discount_expiration[^0-9]{0,24}(\d{10,13})""", RegexOption.IGNORE_CASE),
        ).firstNotNullOfOrNull { it.find(html)?.groupValues?.getOrNull(1) }
        return ParserUtils.parseTimestamp(raw)
    }

    private fun normalizeUrl(product: SteamProduct, region: String): String =
        "https://store.steampowered.com/${product.type}/${product.id}/?cc=${URLEncoder.encode(region.lowercase(), StandardCharsets.UTF_8.name())}&l=spanish"

    private fun cleanTitle(title: String): String = title
        .replace(Regex("""\s+on Steam$""", RegexOption.IGNORE_CASE), "")
        .trim()
}
