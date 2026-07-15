package com.entrecheckpoints.checkpoint.data.analytics

import com.entrecheckpoints.checkpoint.data.model.ProductEdition
import org.junit.Assert.assertEquals
import org.junit.Test

class TitleNormalizerTest {
    @Test
    fun normalizesStoreEditionsToSameKey() {
        assertEquals(
            "resident evil 4",
            TitleNormalizer.comparisonKey("Resident Evil 4 Deluxe Edition - Nintendo Switch"),
        )
        assertEquals(
            "resident evil 4",
            TitleNormalizer.comparisonKey("Resident Evil 4: Gold Edition | Steam"),
        )
    }

    @Test
    fun detectsEditions() {
        assertEquals(ProductEdition.DELUXE, TitleNormalizer.edition("Game Deluxe Edition", "app"))
        assertEquals(ProductEdition.BUNDLE, TitleNormalizer.edition("Game Bundle", "bundle"))
        assertEquals(ProductEdition.DLC, TitleNormalizer.edition("Game Expansion", "app"))
        assertEquals(ProductEdition.REMAKE_REMASTER, TitleNormalizer.edition("Game Remastered", "app"))
    }
}
