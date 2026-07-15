package com.entrecheckpoints.checkpoint.ui.theme

enum class CheckpointThemeMode(
    val id: String,
    val displayName: String,
    val description: String,
) {
    EDITORIAL(
        id = "editorial",
        displayName = "Editorial",
        description = "Crema, tinta y lavanda. La identidad original de Checkpoint.",
    ),
    AFTER_DARK(
        id = "after_dark",
        displayName = "After Dark",
        description = "Carbón, papel nocturno y lavanda eléctrica.",
    ),
    FRUTIGER_AERO(
        id = "frutiger_aero",
        displayName = "Frutiger Aero",
        description = "Cielo, agua, cristal y optimismo tecnológico de 2007.",
    ),
    ARCADE_NEON(
        id = "arcade_neon",
        displayName = "Arcade Neon",
        description = "Negro azulado, cian y magenta con cuadrícula técnica.",
    );

    companion object {
        fun fromId(id: String?): CheckpointThemeMode = entries.firstOrNull { it.id == id } ?: EDITORIAL
    }
}
