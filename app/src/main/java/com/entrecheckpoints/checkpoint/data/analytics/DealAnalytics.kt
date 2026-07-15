package com.entrecheckpoints.checkpoint.data.analytics

import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import kotlin.math.roundToInt

object DealAnalytics {
    enum class ForecastLevel(val displayName: String) {
        IN_OFFER("En oferta ahora"),
        HIGH("Probabilidad alta"),
        MEDIUM("Probabilidad media"),
        LOW("Probabilidad baja"),
        UNKNOWN("Historial insuficiente"),
    }

    data class Result(
        val averagePriceCents: Long,
        val maximumPriceCents: Long,
        val minimumPriceCents: Long,
        val savingsCents: Long,
        val dealScore: Double,
        val dealLabel: String,
        val offerCount: Int,
        val averageDaysBetweenOffers: Int?,
        val daysSinceLastOffer: Int?,
        val forecast: ForecastLevel,
        val forecastDetail: String,
    )

    fun calculate(game: GameEntity, history: List<PricePointEntity>): Result {
        val points = history.sortedBy { it.checkedAt }
        val prices = (points.map { it.priceCents } + game.priceCents).filter { it >= 0 }
        val average = if (prices.isEmpty()) game.priceCents else prices.average().roundToInt().toLong()
        val minimum = prices.minOrNull() ?: game.minPriceCents
        val maximum = prices.maxOrNull() ?: game.regularPriceCents
        val savings = (game.regularPriceCents - game.priceCents).coerceAtLeast(0)

        val lowPosition = if (maximum <= minimum) {
            if (game.priceCents <= minimum) 1.0 else 0.5
        } else {
            (1.0 - ((game.priceCents - minimum).toDouble() / (maximum - minimum).toDouble())).coerceIn(0.0, 1.0)
        }
        val discountScore = (game.discountPercent / 25.0).coerceIn(0.0, 4.0)
        val lowScore = lowPosition * 3.5
        val targetScore = if (game.targetPriceCents != null && game.priceCents <= game.targetPriceCents) 1.5 else 0.0
        val freshnessScore = if (System.currentTimeMillis() - game.lastChecked <= 36 * HOUR) 1.0 else 0.35
        val score = (discountScore + lowScore + targetScore + freshnessScore).coerceIn(0.0, 10.0)
        val label = when {
            score >= 9.0 -> "Compra sobresaliente"
            score >= 7.5 -> "Oferta excelente"
            score >= 6.0 -> "Buena oferta"
            score >= 4.0 -> "Precio razonable"
            else -> "Conviene esperar"
        }

        val starts = mutableListOf<Long>()
        points.forEachIndexed { index, point ->
            val previousDiscount = points.getOrNull(index - 1)?.discountPercent ?: 0
            if (point.discountPercent > 0 && previousDiscount == 0) starts += point.checkedAt
        }
        val intervals = starts.zipWithNext { first, second -> ((second - first) / DAY).toInt().coerceAtLeast(1) }
        val averageInterval = intervals.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
        val lastOffer = points.lastOrNull { it.discountPercent > 0 }?.checkedAt
        val daysSince = lastOffer?.let { ((System.currentTimeMillis() - it).coerceAtLeast(0) / DAY).toInt() }
        val forecast = when {
            game.discountPercent > 0 -> ForecastLevel.IN_OFFER
            averageInterval == null || daysSince == null -> ForecastLevel.UNKNOWN
            daysSince >= averageInterval * 0.85 -> ForecastLevel.HIGH
            daysSince >= averageInterval * 0.50 -> ForecastLevel.MEDIUM
            else -> ForecastLevel.LOW
        }
        val detail = when (forecast) {
            ForecastLevel.IN_OFFER -> "La tienda ya reporta un descuento activo."
            ForecastLevel.HIGH -> "Suele rebajarse aproximadamente cada $averageInterval días y han pasado $daysSince."
            ForecastLevel.MEDIUM -> "Se acerca a su intervalo habitual de $averageInterval días."
            ForecastLevel.LOW -> "La última oferta todavía es reciente respecto a su patrón observado."
            ForecastLevel.UNKNOWN -> "Checkpoint necesita detectar al menos dos ciclos de oferta para estimar un patrón."
        }

        return Result(
            averagePriceCents = average,
            maximumPriceCents = maximum,
            minimumPriceCents = minimum,
            savingsCents = savings,
            dealScore = score,
            dealLabel = label,
            offerCount = starts.size,
            averageDaysBetweenOffers = averageInterval,
            daysSinceLastOffer = daysSince,
            forecast = forecast,
            forecastDetail = detail,
        )
    }

    private const val HOUR = 60L * 60L * 1000L
    private const val DAY = 24L * HOUR
}
