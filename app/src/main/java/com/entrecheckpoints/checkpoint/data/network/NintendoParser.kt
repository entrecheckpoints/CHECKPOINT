package com.entrecheckpoints.checkpoint.data.network

import android.net.Uri
import com.entrecheckpoints.checkpoint.data.model.ProductSnapshot
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.data.network.ParserUtils.decodeHtmlEntities

class NintendoParser(private val http: HttpClient) : StoreParser {
    override val store: Store = Store.NINTENDO

    override fun matches(url: String): Boolean = runCatching {
        val uri = Uri.parse(url)
        uri.host?.endsWith("nintendo.com", ignoreCase = true) == true &&
            uri.path.orEmpty().contains("/store/products/", ignoreCase = true)
    }.getOrDefault(false)

    override suspend fun fetch(url: String): ProductSnapshot {
        val response = http.get(ParserUtils.normalizeUrl(url))
        return parseHtml(response.body, response.finalUrl)
            ?: throw StoreFetchException("Nintendo no devolvió un precio legible. Abre el producto y vuelve a intentarlo.")
    }

    fun parseHtml(html: String, pageUrl: String): ProductSnapshot? {
        val decoded = html.decodeHtmlEntities()
        val purchase = Regex(
            """(?:https?:)?(?:\\?/){2}ec\.nintendo\.com/title_purchase_confirm\?[^\"'<>\s]+""",
            RegexOption.IGNORE_CASE,
        ).find(decoded)?.value?.replace("\\/", "/")
            ?: Regex(
                """(?:href=)?[\"']([^\"']*title_purchase_confirm\?[^\"']+)[\"']""",
                RegexOption.IGNORE_CASE,
            ).find(decoded)?.groupValues?.getOrNull(1)

        val purchaseUri = purchase?.let { raw ->
            runCatching {
                val absolute = when {
                    raw.startsWith("//") -> "https:$raw"
                    raw.startsWith("http") -> raw
                    else -> Uri.parse(pageUrl).buildUpon().encodedPath(raw.substringBefore('?')).encodedQuery(raw.substringAfter('?', "")).build().toString()
                }
                Uri.parse(absolute)
            }.getOrNull()
        }

        val price = ParserUtils.parseMoneyToCents(purchaseUri?.getQueryParameter("v_price"))
            ?: ParserUtils.parseMoneyToCents(ParserUtils.meta(html, "product:price:amount"))
            ?: Regex("""[\"']price[\"']\s*:\s*[\"']?([0-9.,]+)""", RegexOption.IGNORE_CASE)
                .find(decoded)?.groupValues?.getOrNull(1)?.let(ParserUtils::parseMoneyToCents)
            ?: return null

        val region = purchaseUri?.getQueryParameter("v_country")
            ?: ParserUtils.regionFromUrl(pageUrl, "MX")
        val currency = purchaseUri?.getQueryParameter("v_currency")
            ?: ParserUtils.meta(html, "product:price:currency")
            ?: ParserUtils.defaultCurrency(region)
        val productId = purchaseUri?.getQueryParameter("title")
            ?: Regex("""[\"']titleId[\"']\s*:\s*[\"']([0-9]+)""").find(decoded)?.groupValues?.getOrNull(1)

        return ProductSnapshot(
            store = store,
            productId = productId,
            title = cleanTitle(ParserUtils.titleFromHtml(html, "Juego de Nintendo")),
            url = ParserUtils.normalizeUrl(pageUrl),
            imageUrl = ParserUtils.imageFromHtml(html),
            priceCents = price,
            regularPriceCents = price,
            currency = currency,
            region = region.uppercase(),
            offerEndsAt = extractOfferEnd(decoded),
            source = "nintendo-html",
        )
    }

    private fun extractOfferEnd(html: String): Long? {
        val raw = listOf(
            Regex("""[\"'](?:saleEnd|saleEndDate|offerEndDate)[\"']\s*:\s*[\"']([^\"']+)[\"']""", RegexOption.IGNORE_CASE),
            Regex("""[\"'](?:saleEnd|saleEndDate|offerEndDate)[\"']\s*:\s*(\d{10,13})""", RegexOption.IGNORE_CASE),
        ).firstNotNullOfOrNull { it.find(html)?.groupValues?.getOrNull(1) }
        return ParserUtils.parseTimestamp(raw)
    }

    private fun cleanTitle(title: String): String = title
        .replace(Regex("""\s*[|–-]\s*Nintendo.*$""", RegexOption.IGNORE_CASE), "")
        .trim()
}
