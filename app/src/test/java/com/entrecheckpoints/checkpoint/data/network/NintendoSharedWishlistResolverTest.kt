package com.entrecheckpoints.checkpoint.data.network

import org.junit.Assert.assertEquals
import org.junit.Test

class NintendoSharedWishlistResolverTest {
    @Test
    fun parsesJavascriptProductLinks() {
        val raw = """["https://www.nintendo.com/es-mx/store/products/game-one-switch/","https://www.nintendo.com/es-mx/store/products/game-two-switch/?sku=7100007264"]"""
        assertEquals(
            listOf(
                "https://www.nintendo.com/es-mx/store/products/game-one-switch/",
                "https://www.nintendo.com/es-mx/store/products/game-two-switch/?sku=7100007264",
            ),
            NintendoSharedWishlistResolver.parseJavascriptArray(raw),
        )
    }

    @Test
    fun normalizesNintendoProductLinkWithoutDroppingSku() {
        assertEquals(
            "https://www.nintendo.com/es-mx/store/products/game-switch?sku=7100007264",
            NintendoSharedWishlistResolver.normalizeProductUrl(
                "https://www.nintendo.com/es-mx/store/products/game-switch/?sku=7100007264#details",
            ),
        )
    }
}
