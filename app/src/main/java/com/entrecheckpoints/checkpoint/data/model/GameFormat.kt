package com.entrecheckpoints.checkpoint.data.model

enum class GameFormat(val id: String, val displayName: String) {
    DIGITAL("digital", "Digital"),
    PHYSICAL("physical", "Físico");

    companion object {
        fun fromId(id: String?): GameFormat = entries.firstOrNull { it.id == id } ?: DIGITAL
    }
}
