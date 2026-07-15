package com.entrecheckpoints.checkpoint.data.model

enum class Store(
    val id: String,
    val displayName: String,
    val isBeta: Boolean = false,
) {
    NINTENDO("nintendo", "Nintendo eShop"),
    STEAM("steam", "Steam"),
    XBOX("xbox", "Xbox Store", isBeta = true);

    companion object {
        fun fromId(id: String?): Store = entries.firstOrNull { it.id == id } ?: NINTENDO
    }
}
