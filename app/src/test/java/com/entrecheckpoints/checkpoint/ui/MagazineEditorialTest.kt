package com.entrecheckpoints.checkpoint.ui

import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.GameEventEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.model.AlertEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MagazineEditorialTest {
    @Test
    fun `new low event becomes magazine headline`() {
        val game = game(id = "steam-1", price = 34900, regular = 99900, discount = 65)
        val event = GameEventEntity(
            gameId = game.id,
            type = AlertEventType.NEW_LOW.id,
            title = "Nuevo mínimo",
            oldPriceCents = 49900,
            newPriceCents = 34900,
            discountPercent = 65,
            currency = "MXN",
            createdAt = System.currentTimeMillis(),
            detail = "Bajó a su menor precio registrado.",
        )

        val feature = MagazineEditorial.selectFeature(listOf(game), emptyMap(), listOf(event))

        assertNotNull(feature)
        assertEquals("NEW\nHISTORICAL\nLOW", feature?.headline)
        assertEquals("NUEVO MÍNIMO", feature?.kicker)
    }

    @Test
    fun `movement compares two newest history points`() {
        val down = game(id = "down", price = 30000, regular = 60000)
        val up = game(id = "up", price = 50000, regular = 60000)
        val stable = game(id = "stable", price = 40000, regular = 60000, status = "error")
        val history = mapOf(
            down.id to listOf(point(down.id, 50000, 1), point(down.id, 30000, 2)),
            up.id to listOf(point(up.id, 30000, 1), point(up.id, 50000, 2)),
            stable.id to listOf(point(stable.id, 40000, 1), point(stable.id, 40000, 2)),
        )

        val movement = MagazineEditorial.movement(listOf(down, up, stable), history)

        assertEquals(1, movement.down)
        assertEquals(1, movement.up)
        assertEquals(1, movement.stable)
        assertEquals(1, movement.errors)
    }

    @Test
    fun `daily issue includes a stable barcode seed`() {
        val issue = MagazineEditorial.issueData(1_720_000_000_000L)

        assertTrue(issue.number.length == 3)
        assertTrue(issue.dateLabel.isNotBlank())
        assertTrue(issue.barcodeSeed.startsWith("CP-${issue.number}-"))
    }

    private fun game(
        id: String,
        price: Long,
        regular: Long,
        discount: Int = 0,
        status: String = "ok",
    ) = GameEntity(
        id = id,
        storeId = "steam",
        productId = id,
        productType = "app",
        title = "Juego $id",
        url = "https://store.steampowered.com/app/$id",
        imageUrl = null,
        priceCents = price,
        regularPriceCents = regular,
        currency = "MXN",
        region = "MX",
        discountPercent = discount,
        minPriceCents = price,
        targetPriceCents = null,
        addedAt = 1,
        lastChecked = 2,
        lastStatus = status,
        lastError = null,
        source = "test",
    )

    private fun point(gameId: String, price: Long, checkedAt: Long) = PricePointEntity(
        gameId = gameId,
        priceCents = price,
        regularPriceCents = 60000,
        discountPercent = 0,
        checkedAt = checkedAt,
        source = "test",
    )
}
