package com.entrecheckpoints.checkpoint.data.analytics

import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class DealAnalyticsTest {
    @Test
    fun scoresCurrentMinimumAsStrongDeal() {
        val now = System.currentTimeMillis()
        val game = GameEntity(
            id = "steam-1",
            storeId = "steam",
            productId = "1",
            productType = "app",
            title = "Example",
            url = "https://store.steampowered.com/app/1",
            imageUrl = null,
            priceCents = 30_000,
            regularPriceCents = 100_000,
            currency = "MXN",
            region = "MX",
            discountPercent = 70,
            minPriceCents = 30_000,
            targetPriceCents = 35_000,
            addedAt = now,
            lastChecked = now,
            lastStatus = "ok",
            lastError = null,
            source = "test",
        )
        val history = listOf(
            PricePointEntity(gameId = game.id, priceCents = 100_000, regularPriceCents = 100_000, discountPercent = 0, checkedAt = now - 10_000, source = "test"),
            PricePointEntity(gameId = game.id, priceCents = 30_000, regularPriceCents = 100_000, discountPercent = 70, checkedAt = now, source = "test"),
        )
        val result = DealAnalytics.calculate(game, history)
        assertTrue(result.dealScore >= 8.0)
        assertTrue(result.savingsCents == 70_000L)
    }
}
