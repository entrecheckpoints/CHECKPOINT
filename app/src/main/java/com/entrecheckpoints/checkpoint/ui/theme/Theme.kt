package com.entrecheckpoints.checkpoint.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CheckpointPattern {
    PAPER_GRID,
    NIGHT_LINES,
    AERO_BUBBLES,
    NEON_GRID,
}

@Immutable
data class CheckpointVisuals(
    val mode: CheckpointThemeMode,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceAlt: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val accentSecondary: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val onAccentSecondary: Color,
    val hairline: Color,
    val borderStrong: Color,
    val success: Color,
    val warning: Color,
    val pattern: CheckpointPattern,
    val cornerRadius: Dp,
    val coverSaturation: Float,
    val nintendo: Color,
    val steam: Color,
    val xbox: Color,
)

private val EditorialVisuals = CheckpointVisuals(
    mode = CheckpointThemeMode.EDITORIAL,
    background = Cream,
    surface = PaperLight,
    surfaceRaised = PaperRaised,
    surfaceAlt = PaperAlt,
    ink = Ink,
    muted = InkMuted,
    accent = Lavender,
    accentSecondary = LavenderDark,
    accentSoft = Lavender.copy(alpha = 0.18f),
    onAccent = Ink,
    onAccentSecondary = Color.White,
    hairline = Hairline,
    borderStrong = StrongHairline,
    success = Color(0xFF236A3B),
    warning = Color(0xFF925600),
    pattern = CheckpointPattern.PAPER_GRID,
    cornerRadius = 0.dp,
    coverSaturation = 0f,
    nintendo = NintendoRed,
    steam = SteamBlue,
    xbox = XboxGreen,
)

private val DarkVisuals = CheckpointVisuals(
    mode = CheckpointThemeMode.AFTER_DARK,
    background = NightBackground,
    surface = NightSurface,
    surfaceRaised = NightRaised,
    surfaceAlt = NightSurfaceAlt,
    ink = NightInk,
    muted = NightMuted,
    accent = NightLavender,
    accentSecondary = NightLavenderDark,
    accentSoft = NightLavender.copy(alpha = 0.13f),
    onAccent = Color(0xFF17111D),
    onAccentSecondary = Color(0xFF120D18),
    hairline = Color(0x3DF4F0E7),
    borderStrong = Color(0x70F4F0E7),
    success = Color(0xFF77D28A),
    warning = Color(0xFFFFB960),
    pattern = CheckpointPattern.NIGHT_LINES,
    cornerRadius = 6.dp,
    coverSaturation = 0.10f,
    nintendo = Color(0xFFFF6B74),
    steam = Color(0xFF78CFF4),
    xbox = Color(0xFF7BDB73),
)

private val AeroVisuals = CheckpointVisuals(
    mode = CheckpointThemeMode.FRUTIGER_AERO,
    background = AeroSky,
    surface = AeroSurface,
    surfaceRaised = AeroRaised,
    surfaceAlt = AeroSurfaceAlt,
    ink = AeroInk,
    muted = AeroMuted,
    accent = AeroCyan,
    accentSecondary = AeroGreen,
    accentSoft = AeroCyan.copy(alpha = 0.13f),
    onAccent = Color.White,
    onAccentSecondary = Color.White,
    hairline = Color(0x3307364D),
    borderStrong = Color(0x6607364D),
    success = Color(0xFF237E34),
    warning = Color(0xFF915500),
    pattern = CheckpointPattern.AERO_BUBBLES,
    cornerRadius = 16.dp,
    coverSaturation = 0.92f,
    nintendo = Color(0xFFD91C2B),
    steam = Color(0xFF066B91),
    xbox = Color(0xFF287D28),
)

private val NeonVisuals = CheckpointVisuals(
    mode = CheckpointThemeMode.ARCADE_NEON,
    background = NeonBackground,
    surface = NeonSurface,
    surfaceRaised = NeonRaised,
    surfaceAlt = NeonSurfaceAlt,
    ink = NeonInk,
    muted = NeonMuted,
    accent = NeonCyan,
    accentSecondary = NeonMagenta,
    accentSoft = NeonCyan.copy(alpha = 0.11f),
    onAccent = Color(0xFF041014),
    onAccentSecondary = Color(0xFF160411),
    hairline = NeonCyan.copy(alpha = 0.22f),
    borderStrong = NeonCyan.copy(alpha = 0.52f),
    success = Color(0xFF57E389),
    warning = Color(0xFFFFBE55),
    pattern = CheckpointPattern.NEON_GRID,
    cornerRadius = 4.dp,
    coverSaturation = 0.92f,
    nintendo = Color(0xFFFF5D78),
    steam = NeonCyan,
    xbox = Color(0xFF57E389),
)

private val LocalCheckpointVisuals = staticCompositionLocalOf { EditorialVisuals }

object CheckpointStyle {
    val current: CheckpointVisuals
        @Composable
        @ReadOnlyComposable
        get() = LocalCheckpointVisuals.current
}

@Composable
fun checkpointShape(): Shape = RoundedCornerShape(CheckpointStyle.current.cornerRadius)

fun checkpointVisualsFor(mode: CheckpointThemeMode): CheckpointVisuals = when (mode) {
    CheckpointThemeMode.EDITORIAL -> EditorialVisuals
    CheckpointThemeMode.AFTER_DARK -> DarkVisuals
    CheckpointThemeMode.FRUTIGER_AERO -> AeroVisuals
    CheckpointThemeMode.ARCADE_NEON -> NeonVisuals
}

private fun colorSchemeFor(visuals: CheckpointVisuals) = when (visuals.mode) {
    CheckpointThemeMode.EDITORIAL,
    CheckpointThemeMode.FRUTIGER_AERO,
    -> lightColorScheme(
        primary = visuals.accent,
        onPrimary = visuals.onAccent,
        primaryContainer = visuals.accentSoft,
        onPrimaryContainer = visuals.ink,
        secondary = visuals.accentSecondary,
        onSecondary = visuals.onAccentSecondary,
        secondaryContainer = visuals.surfaceAlt,
        onSecondaryContainer = visuals.ink,
        background = visuals.background,
        onBackground = visuals.ink,
        surface = visuals.surface,
        onSurface = visuals.ink,
        surfaceVariant = visuals.surfaceAlt,
        onSurfaceVariant = visuals.muted,
        outline = visuals.borderStrong,
        outlineVariant = visuals.hairline,
        error = ErrorRed,
        onError = Color.White,
    )

    CheckpointThemeMode.AFTER_DARK,
    CheckpointThemeMode.ARCADE_NEON,
    -> darkColorScheme(
        primary = visuals.accent,
        onPrimary = visuals.onAccent,
        primaryContainer = visuals.accentSoft,
        onPrimaryContainer = visuals.ink,
        secondary = visuals.accentSecondary,
        onSecondary = visuals.onAccentSecondary,
        secondaryContainer = visuals.surfaceAlt,
        onSecondaryContainer = visuals.ink,
        background = visuals.background,
        onBackground = visuals.ink,
        surface = visuals.surface,
        onSurface = visuals.ink,
        surfaceVariant = visuals.surfaceAlt,
        onSurfaceVariant = visuals.muted,
        outline = visuals.borderStrong,
        outlineVariant = visuals.hairline,
        error = Color(0xFFFF7A85),
        onError = Color(0xFF230005),
    )
}

@Composable
fun CheckpointTheme(
    mode: CheckpointThemeMode = CheckpointThemeMode.EDITORIAL,
    content: @Composable () -> Unit,
) {
    val visuals = checkpointVisualsFor(mode)
    androidx.compose.runtime.CompositionLocalProvider(LocalCheckpointVisuals provides visuals) {
        MaterialTheme(
            colorScheme = colorSchemeFor(visuals),
            typography = CheckpointTypography,
            content = content,
        )
    }
}
