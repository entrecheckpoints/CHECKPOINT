package com.entrecheckpoints.checkpoint.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {
    @Test
    fun allThemesKeepPrimaryAndMutedTextReadable() {
        CheckpointThemeMode.entries.forEach { mode ->
            val theme = checkpointVisualsFor(mode)
            assertTrue(
                "${mode.name}: ink sobre surfaceRaised",
                contrastRatio(theme.ink, theme.surfaceRaised) >= 7.0,
            )
            assertTrue(
                "${mode.name}: muted sobre surfaceRaised",
                contrastRatio(theme.muted, theme.surfaceRaised) >= 4.5,
            )
            assertTrue(
                "${mode.name}: texto sobre accent",
                contrastRatio(theme.onAccent, theme.accent) >= 4.5,
            )
            assertTrue(
                "${mode.name}: texto sobre accentSecondary",
                contrastRatio(theme.onAccentSecondary, theme.accentSecondary) >= 4.5,
            )
            listOf(theme.nintendo, theme.steam, theme.xbox).forEachIndexed { index, storeColor ->
                assertTrue(
                    "${mode.name}: color de tienda $index sobre surfaceRaised",
                    contrastRatio(storeColor, theme.surfaceRaised) >= 4.5,
                )
            }
        }
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val firstLuminance = relativeLuminance(first)
        val secondLuminance = relativeLuminance(second)
        val lighter = maxOf(firstLuminance, secondLuminance)
        val darker = minOf(firstLuminance, secondLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun channel(value: Float): Double {
            val component = value.toDouble()
            return if (component <= 0.04045) component / 12.92
            else Math.pow((component + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(color.red) +
            0.7152 * channel(color.green) +
            0.0722 * channel(color.blue)
    }
}
