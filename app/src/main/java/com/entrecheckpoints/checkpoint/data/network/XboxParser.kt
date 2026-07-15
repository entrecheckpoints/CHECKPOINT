package com.entrecheckpoints.checkpoint.data.network

import android.net.Uri
import com.entrecheckpoints.checkpoint.data.model.ProductSnapshot
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.data.model.SubscriptionService
import com.entrecheckpoints.checkpoint.data.network.ParserUtils.decodeHtmlEntities

class XboxParser(private val http: HttpClient) : StoreParser {
    override val store: Store = Store.XBOX

    override fun matches(url: String): Boolean = productId(url) != null

    fun productId(url: String): String? {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return null
        if (uri.host?.endsWith("xbox.com", ignoreCase = true) != true ||
            !uri.path.orEmpty().contains("/games/store/", ignoreCase = true)
        ) return null
        return uri.pathSegments.firstOrNull { it.matches(Regex("[A-Za-z0-9]{12}")) }?.uppercase()
    }

    override suspend fun fetch(url: String): ProductSnapshot {
        val normalized = ParserUtils.normalizeUrl(url).substringBefore('?')
        val response = http.get(normalized)
        return parseHtml(response.body, response.finalUrl)
            ?: throw StoreFetchException("Xbox cambió o retrasó sus datos de precio. La tienda queda marcada como beta por exactamente este tipo de tonterías.")
    }

    fun parseHtml(html: String, pageUrl: String): ProductSnapshot? {
        val id = productId(pageUrl) ?: return null
        val decoded = html.decodeHtmlEntities()
        val region = ParserUtils.regionFromUrl(pageUrl, "MX")
        val currency = Regex("""[\"'](?:currency|currencyCode)[\"']\s*:\s*[\"']([A-Z]{3})[\"']""")
            .find(decoded)?.groupValues?.getOrNull(1)
            ?: ParserUtils.meta(html, "product:price:currency")
            ?: ParserUtils.defaultCurrency(region)

        val current = firstCents(
            decoded,
            listOf(
                """[\"'](?:discountedPrice|currentPrice|salePrice|finalPrice)[\"']\s*:\s*(?:\{[^}]*[\"']value[\"']\s*:\s*)?[\"']?([0-9]+(?:\.[0-9]+)?)""",
                """[\"']price[\"']\s*:\s*\{[^}]*[\"'](?:current|value)[\"']\s*:\s*[\"']?([0-9]+(?:\.[0-9]+)?)""",
            ),
        ) ?: ParserUtils.parseMoneyToCents(ParserUtils.meta(html, "product:price:amount"))
            ?: visiblePrice(decoded)
            ?: return null

        val regular = firstCents(
            decoded,
            listOf("""[\"'](?:listPrice|originalPrice|msrp)[\"']\s*:\s*(?:\{[^}]*[\"']value[\"']\s*:\s*)?[\"']?([0-9]+(?:\.[0-9]+)?)"""),
        ) ?: current

        val percent = Regex("""-\s*(\d{1,3})\s*%""").find(decoded)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: ParserUtils.discountPercent(regular, current)

        return ProductSnapshot(
            store = store,
            productId = id,
            title = cleanTitle(ParserUtils.titleFromHtml(html, "Juego de Xbox")),
            url = ParserUtils.normalizeUrl(pageUrl).substringBefore('?'),
            imageUrl = ParserUtils.imageFromHtml(html),
            priceCents = current,
            regularPriceCents = regular,
            currency = currency,
            region = region,
            discountPercent = percent,
            offerEndsAt = extractOfferEnd(decoded),
            detectedSubscriptions = buildSet {
                if (Regex("""\b(Included with|Incluido con|Get it with|Consíguelo con) Game Pass\b""", RegexOption.IGNORE_CASE).containsMatchIn(decoded)) add(SubscriptionService.GAME_PASS)
                if (Regex("""\b(Included with|Incluido con) EA Play\b""", RegexOption.IGNORE_CASE).containsMatchIn(decoded)) add(SubscriptionService.EA_PLAY)
                if (Regex("""\b(Included with|Incluido con) Ubisoft\+\b""", RegexOption.IGNORE_CASE).containsMatchIn(decoded)) add(SubscriptionService.UBISOFT_PLUS)
            },
            source = "xbox-html-beta",
        )
    }

    private fun extractOfferEnd(html: String): Long? {
        val raw = listOf(
            Regex("""[\"'](?:endDate|endTime|discountEndDate)[\"']\s*:\s*[\"']([^\"']+)[\"']""", RegexOption.IGNORE_CASE),
            Regex("""[\"'](?:endDate|endTime|discountEndDate)[\"']\s*:\s*(\d{10,13})""", RegexOption.IGNORE_CASE),
        ).firstNotNullOfOrNull { it.find(html)?.groupValues?.getOrNull(1) }
        return ParserUtils.parseTimestamp(raw)
    }

    private fun firstCents(html: String, patterns: List<String>): Long? = patterns.firstNotNullOfOrNull { pattern ->
        Regex(pattern, RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1)?.let(ParserUtils::parseMoneyToCents)
    }

    private fun visiblePrice(html: String): Long? {
        if (Regex("""\bGratis\b""", RegexOption.IGNORE_CASE).containsMatchIn(html.take(150_000))) return 0
        val focusStart = html.indexOf("<h1", ignoreCase = true).coerceAtLeast(0)
        val focus = ParserUtils.cleanText(html.substring(focusStart, (focusStart + 80_000).coerceAtMost(html.length)))
        return Regex("""(?:MXN|MX\$|\$)\s*([0-9][0-9.,]*)""", RegexOption.IGNORE_CASE)
            .find(focus)?.groupValues?.getOrNull(1)?.let(ParserUtils::parseMoneyToCents)
    }

    private fun cleanTitle(title: String): String = title
        .replace(Regex("""^Comprar\s+""", RegexOption.IGNORE_CASE), "")
        .replace(Regex("""\s*[|–-]\s*Xbox.*$""", RegexOption.IGNORE_CASE), "")
        .trim()
}
