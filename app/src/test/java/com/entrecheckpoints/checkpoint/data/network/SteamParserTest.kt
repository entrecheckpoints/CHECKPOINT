package com.entrecheckpoints.checkpoint.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SteamParserTest {
    @Test
    fun parsesSteamAppDetailsPayload() {
        val json = """
            {
              "1551360": {
                "success": true,
                "data": {
                  "name": "Forza Horizon 5",
                  "header_image": "https://example.com/header.jpg",
                  "is_free": false,
                  "price_overview": {
                    "currency": "MXN",
                    "initial": 139900,
                    "final": 69950,
                    "discount_percent": 50
                  }
                }
              }
            }
        """.trimIndent()
        val parser = SteamParser(HttpClient())
        val result = parser.parseApi(json, SteamParser.SteamProduct("app", "1551360"), "MX")
        assertNotNull(result)
        assertEquals("Forza Horizon 5", result?.title)
        assertEquals(69_950L, result?.priceCents)
        assertEquals(139_900L, result?.regularPriceCents)
        assertEquals(50, result?.discountPercent)
    }
}
