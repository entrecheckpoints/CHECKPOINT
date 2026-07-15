package com.entrecheckpoints.checkpoint.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class CheckpointThemeModeTest {
    @Test
    fun readsKnownThemeIds() {
        assertEquals(CheckpointThemeMode.EDITORIAL, CheckpointThemeMode.fromId("editorial"))
        assertEquals(CheckpointThemeMode.AFTER_DARK, CheckpointThemeMode.fromId("after_dark"))
        assertEquals(CheckpointThemeMode.FRUTIGER_AERO, CheckpointThemeMode.fromId("frutiger_aero"))
        assertEquals(CheckpointThemeMode.ARCADE_NEON, CheckpointThemeMode.fromId("arcade_neon"))
    }

    @Test
    fun fallsBackToEditorialForUnknownValues() {
        assertEquals(CheckpointThemeMode.EDITORIAL, CheckpointThemeMode.fromId(null))
        assertEquals(CheckpointThemeMode.EDITORIAL, CheckpointThemeMode.fromId("human_error_beige"))
    }
}
