package com.entrecheckpoints.checkpoint.ui

import com.entrecheckpoints.checkpoint.data.analytics.DealAnalytics
import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.GameEventEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.model.AlertEventType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Lógica editorial pura y comprobable sin Compose. */
object MagazineEditorial {
    fun selectFeature(
        games: List<GameEntity>,
        historyByGame: Map<String, List<PricePointEntity>>,
        events: List<GameEventEntity>,
    ): MagazineFeature? {
        if (games.isEmpty()) return null
        val recentCutoff = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        val latestEvents = events
            .filter { it.createdAt >= recentCutoff }
            .groupBy { it.gameId }
            .mapValues { (_, values) -> values.maxByOrNull { it.createdAt } }
        val game = games.maxByOrNull { candidate ->
            val analytics = DealAnalytics.calculate(candidate, historyByGame[candidate.id].orEmpty())
            val latest = latestEvents[candidate.id]
            val eventBonus = when (latest?.let { AlertEventType.fromId(it.type) }) {
                AlertEventType.NEW_LOW -> 4.0
                AlertEventType.TARGET_REACHED -> 3.6
                AlertEventType.OFFER_ENDING -> 2.8
                AlertEventType.OFFER_RETURNED -> 2.2
                AlertEventType.PRICE_DROP,
                AlertEventType.DISCOUNT_THRESHOLD,
                AlertEventType.DROP_AMOUNT,
                -> 1.6
                else -> 0.0
            }
            val targetBonus = if (candidate.targetPriceCents != null && candidate.priceCents <= candidate.targetPriceCents) 2.0 else 0.0
            analytics.dealScore + eventBonus + targetBonus + candidate.discountPercent / 20.0
        } ?: return null
        val analytics = DealAnalytics.calculate(game, historyByGame[game.id].orEmpty())
        val latest = latestEvents[game.id]
        val event = latest?.let { AlertEventType.fromId(it.type) }
        val headline = when {
            event == AlertEventType.NEW_LOW || game.priceCents <= analytics.minimumPriceCents -> "NEW\nHISTORICAL\nLOW"
            event == AlertEventType.TARGET_REACHED || (game.targetPriceCents != null && game.priceCents <= game.targetPriceCents) -> "TARGET\nREACHED"
            event == AlertEventType.OFFER_ENDING -> "ENDING\nSOON"
            event == AlertEventType.OFFER_RETURNED -> "THE DEAL\nIS BACK"
            game.discountPercent >= 50 -> "MASSIVE\nPRICE DROP"
            game.discountPercent > 0 -> "BEST DEAL\nTODAY"
            else -> "WORTH\nWATCHING"
        }
        val kicker = when {
            event == AlertEventType.OFFER_ENDING -> "ÚLTIMA LLAMADA"
            event == AlertEventType.TARGET_REACHED -> "OBJETIVO ALCANZADO"
            event == AlertEventType.NEW_LOW -> "NUEVO MÍNIMO"
            game.discountPercent > 0 -> "OFERTA DESTACADA"
            else -> "PORTADA DEL DÍA"
        }
        val detail = when {
            game.offerEndsAt != null -> "Válido hasta ${shortDate(game.offerEndsAt)}"
            latest != null -> latest.detail.ifBlank { analytics.dealLabel }
            else -> "${analytics.dealLabel} · Deal Score ${String.format(Locale.US, "%.1f", analytics.dealScore)}"
        }
        return MagazineFeature(game, headline, kicker, detail)
    }

    fun issueData(now: Long = System.currentTimeMillis()): MagazineIssue {
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        val issue = calendar.get(Calendar.DAY_OF_YEAR).toString().padStart(3, '0')
        val label = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-MX"))
            .format(Date(now))
            .uppercase(Locale.forLanguageTag("es-MX"))
        val compact = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(now))
        return MagazineIssue(issue, label, "CP-$issue-$compact")
    }

    fun movement(
        games: List<GameEntity>,
        historyByGame: Map<String, List<PricePointEntity>>,
    ): PriceMovement {
        var down = 0
        var up = 0
        var stable = 0
        games.forEach { game ->
            val recent = historyByGame[game.id].orEmpty().sortedByDescending { it.checkedAt }.take(2)
            if (recent.size < 2) {
                stable++
            } else {
                when {
                    recent[0].priceCents < recent[1].priceCents -> down++
                    recent[0].priceCents > recent[1].priceCents -> up++
                    else -> stable++
                }
            }
        }
        return PriceMovement(down, up, stable, games.count { it.lastStatus == "error" })
    }

    fun pulsePoints(
        games: List<GameEntity>,
        historyByGame: Map<String, List<PricePointEntity>>,
    ): List<Float> {
        val timestamps = historyByGame.values.flatten().map { it.checkedAt }.distinct().sorted().takeLast(18)
        if (timestamps.isEmpty()) return emptyList()
        return timestamps.map { timestamp ->
            val ratios = games.mapNotNull { game ->
                val point = historyByGame[game.id].orEmpty()
                    .filter { it.checkedAt <= timestamp }
                    .maxByOrNull { it.checkedAt }
                    ?: return@mapNotNull null
                val base = point.regularPriceCents.coerceAtLeast(1L)
                point.priceCents.toFloat() / base.toFloat()
            }
            if (ratios.isEmpty()) 1f else ratios.average().toFloat()
        }
    }

    private fun shortDate(timestamp: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-MX")).format(Date(timestamp))
}

data class MagazineFeature(
    val game: GameEntity,
    val headline: String,
    val kicker: String,
    val detail: String,
)

data class MagazineIssue(
    val number: String,
    val dateLabel: String,
    val barcodeSeed: String,
)

data class PriceMovement(
    val down: Int,
    val up: Int,
    val stable: Int,
    val errors: Int,
)
