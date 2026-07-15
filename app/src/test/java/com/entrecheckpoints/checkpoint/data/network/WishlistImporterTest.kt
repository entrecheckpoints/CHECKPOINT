package com.entrecheckpoints.checkpoint.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WishlistImporterTest {
    @Test
    fun extractsIdsFromLegacyWishlistJson() {
        val payload = """
            {
              "620": {"name": "Portal 2"},
              "1245620": {"name": "ELDEN RING"}
            }
        """.trimIndent()

        assertEquals(setOf("620", "1245620"), WishlistImporter.extractSteamAppIds(payload))
    }

    @Test
    fun extractsIdsFromNestedAndHtmlPayloads() {
        val payload = """
            {"response":{"items":[{"appid":1551360},{"appid":"367520"}]}}
            <a href="https://store.steampowered.com/app/413150/">Game</a>
            <div data-app-id="220"></div>
        """.trimIndent()

        val ids = WishlistImporter.extractSteamAppIds(payload)
        assertTrue(ids.containsAll(listOf("1551360", "367520", "413150", "220")))
    }

    @Test
    fun convertsSteamCommunityProfileToWishlistBase() {
        assertEquals(
            listOf("https://store.steampowered.com/wishlist/profiles/76561198000000000"),
            WishlistImporter.steamWishlistBases("https://steamcommunity.com/profiles/76561198000000000/"),
        )
    }

    @Test
    fun preservesVanityWishlistIdentity() {
        assertEquals(
            listOf("https://store.steampowered.com/wishlist/id/example_user"),
            WishlistImporter.steamWishlistBases(
                "https://store.steampowered.com/wishlist/id/example_user/#sort=order",
            ),
        )
    }

    @Test
    fun parsesNintendoSharedWishlistSkusAndRegion() {
        val parsed = WishlistImporter.parseNintendoSharedWishlist(
            "https://www.nintendo.com/es-mx/wish-list/share/#" +
                "skus=7100096976,7100007264,7100096976&date=1784149072000",
        )

        requireNotNull(parsed)
        assertEquals("es-mx", parsed.locale)
        assertEquals("MX", parsed.region)
        assertEquals(listOf("7100096976", "7100007264"), parsed.skus)
        assertEquals(1784149072000L, parsed.createdAtMillis)
    }

    @Test
    fun rejectsNintendoShareWithoutSkus() {
        val parsed = WishlistImporter.parseNintendoSharedWishlist(
            "https://www.nintendo.com/es-mx/wish-list/share/#date=1784149072000",
        )
        assertEquals(null, parsed)
    }
}
