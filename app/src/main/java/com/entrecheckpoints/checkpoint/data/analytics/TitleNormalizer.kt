package com.entrecheckpoints.checkpoint.data.analytics

import com.entrecheckpoints.checkpoint.data.model.ProductEdition
import java.text.Normalizer
import java.util.Locale

object TitleNormalizer {
    private val editionWords = Regex(
        """\b(deluxe|ultimate|gold|complete|goty|game of the year|collector'?s?|premium|definitive|standard|edition|bundle|pack|collection|anthology|remastered|remake|upgrade|dlc|expansion|season pass|digital)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val platformWords = Regex(
        """\b(nintendo switch|switch 2|xbox series x\|s|xbox series|xbox one|playstation 5|playstation 4|ps5|ps4|steam|pc)\b""",
        RegexOption.IGNORE_CASE,
    )

    fun comparisonKey(title: String): String {
        val normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
            .replace("®", " ")
            .replace("™", " ")
            .replace(platformWords, " ")
            .replace(editionWords, " ")
            .replace(Regex("""[\[\](){}:|–—\-_/]+"""), " ")
            .replace(Regex("""\bfor\b"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
        return normalized.ifBlank { title.lowercase(Locale.ROOT).trim() }
    }

    fun edition(title: String, productType: String?): ProductEdition {
        val value = "$title ${productType.orEmpty()}".lowercase(Locale.ROOT)
        return when {
            Regex("""\b(dlc|expansion|season pass)\b""").containsMatchIn(value) -> ProductEdition.DLC
            Regex("""\b(upgrade|upgrade pack)\b""").containsMatchIn(value) -> ProductEdition.UPGRADE
            Regex("""\b(bundle|pack|collection|anthology)\b""").containsMatchIn(value) || productType == "bundle" || productType == "sub" -> ProductEdition.BUNDLE
            Regex("""\b(complete|goty|game of the year|definitive)\b""").containsMatchIn(value) -> ProductEdition.COMPLETE
            Regex("""\b(remake|remastered|remaster)\b""").containsMatchIn(value) -> ProductEdition.REMAKE_REMASTER
            Regex("""\b(deluxe|ultimate|gold|premium|collector)\b""").containsMatchIn(value) -> ProductEdition.DELUXE
            else -> ProductEdition.BASE
        }
    }
}
