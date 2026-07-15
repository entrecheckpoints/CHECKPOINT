package com.entrecheckpoints.checkpoint.data.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ParserUtilsTest {
    @Test
    fun parsesMexicanPrices() {
        assertEquals(122_900L, ParserUtils.parseMoneyToCents("$1,229.00"))
        assertEquals(122_900L, ParserUtils.parseMoneyToCents("1.229,00 MXN"))
        assertEquals(27_300L, ParserUtils.parseMoneyToCents("273"))
        assertEquals(0L, ParserUtils.parseMoneyToCents("Gratis"))
    }

    @Test
    fun calculatesDiscounts() {
        assertEquals(50, ParserUtils.discountPercent(100_000L, 50_000L))
        assertEquals(0, ParserUtils.discountPercent(50_000L, 50_000L))
    }
}
