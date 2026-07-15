package com.entrecheckpoints.checkpoint.data.network

import android.net.Uri
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.util.Locale
import java.time.Instant
import java.time.OffsetDateTime

object ParserUtils {
    private val tags = Regex("<[^>]+>")
    private val spaces = Regex("\\s+")

    fun cleanText(value: String?): String = value.orEmpty()
        .replace(tags, " ")
        .decodeHtmlEntities()
        .replace(spaces, " ")
        .trim()

    fun String.decodeHtmlEntities(): String = this
        .replace("&amp;", "&", ignoreCase = true)
        .replace("&quot;", "\"", ignoreCase = true)
        .replace("&#39;", "'", ignoreCase = true)
        .replace("&apos;", "'", ignoreCase = true)
        .replace("&lt;", "<", ignoreCase = true)
        .replace("&gt;", ">", ignoreCase = true)
        .replace("&nbsp;", " ", ignoreCase = true)
        .replace("\\u0026", "&")
        .replace("\\/", "/")

    fun parseMoneyToCents(raw: String?): Long? {
        var value = raw?.trim()?.replace("\u00A0", " ") ?: return null
        if (value.isBlank()) return null
        if (Regex("\\b(gratis|free)\\b", RegexOption.IGNORE_CASE).containsMatchIn(value)) return 0
        value = value.replace(Regex("[^0-9,.-]"), "")
        if (value.isBlank() || value == "-" || value == "." || value == ",") return null

        val lastComma = value.lastIndexOf(',')
        val lastDot = value.lastIndexOf('.')
        val decimalSeparator = when {
            lastComma >= 0 && lastDot >= 0 -> if (lastComma > lastDot) ',' else '.'
            lastComma >= 0 && value.length - lastComma - 1 in 1..2 -> ','
            lastDot >= 0 && value.length - lastDot - 1 in 1..2 -> '.'
            else -> null
        }
        val normalized = buildString {
            value.forEach { char ->
                when {
                    char.isDigit() || char == '-' -> append(char)
                    char == decimalSeparator -> append('.')
                }
            }
        }
        return runCatching {
            BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
        }.getOrNull()
    }

    fun decimalToCents(value: Any?): Long? = when (value) {
        null -> null
        is Number -> BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP).movePointRight(2).toLong()
        else -> parseMoneyToCents(value.toString())
    }

    fun centsToDecimal(cents: Long): Double = cents / 100.0

    fun regionFromUrl(rawUrl: String, fallback: String = "MX"): String {
        val path = runCatching { URL(rawUrl).path }.getOrDefault("")
        val segment = path.split('/').firstOrNull { it.matches(Regex("[a-z]{2}-[a-z]{2}", RegexOption.IGNORE_CASE)) }
        return segment?.substringAfter('-')?.uppercase(Locale.ROOT) ?: fallback
    }

    fun defaultCurrency(region: String): String = when (region.uppercase(Locale.ROOT)) {
        "MX" -> "MXN"
        "US" -> "USD"
        "CA" -> "CAD"
        "AR" -> "ARS"
        "BR" -> "BRL"
        "CL" -> "CLP"
        "CO" -> "COP"
        "PE" -> "PEN"
        "GB" -> "GBP"
        "JP" -> "JPY"
        else -> "EUR"
    }

    fun meta(html: String, key: String): String? {
        val escaped = Regex.escape(key)
        val patterns = listOf(
            Regex("<meta[^>]+(?:property|name|itemprop)=[\\\"']$escaped[\\\"'][^>]+content=[\\\"']([^\\\"']+)", RegexOption.IGNORE_CASE),
            Regex("<meta[^>]+content=[\\\"']([^\\\"']+)[\\\"'][^>]+(?:property|name|itemprop)=[\\\"']$escaped[\\\"']", RegexOption.IGNORE_CASE),
        )
        return patterns.firstNotNullOfOrNull { it.find(html)?.groupValues?.getOrNull(1)?.decodeHtmlEntities() }
    }

    fun titleFromHtml(html: String, fallback: String): String {
        val og = meta(html, "og:title")
        val title = Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .find(html)?.groupValues?.getOrNull(1)
        return cleanText(og ?: title ?: fallback)
    }

    fun imageFromHtml(html: String): String? = meta(html, "og:image")

    fun normalizeUrl(rawUrl: String): String = runCatching {
        val uri = Uri.parse(rawUrl)
        uri.buildUpon().fragment(null).build().toString()
    }.getOrDefault(rawUrl.trim())

    fun idFrom(value: String): String {
        var hash = 5381L
        value.forEach { hash = ((hash shl 5) + hash) xor it.code.toLong() }
        return java.lang.Long.toUnsignedString(hash, 36)
    }

    fun parseTimestamp(raw: String?): Long? {
        val value = raw?.trim()?.trim('"', '\'')?.takeIf(String::isNotBlank) ?: return null
        value.toLongOrNull()?.let { numeric ->
            return if (numeric < 10_000_000_000L) numeric * 1000L else numeric
        }
        return runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
    }

    fun discountPercent(regularCents: Long, priceCents: Long): Int =
        if (regularCents > priceCents && regularCents > 0) {
            ((1.0 - priceCents.toDouble() / regularCents.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else 0
}
