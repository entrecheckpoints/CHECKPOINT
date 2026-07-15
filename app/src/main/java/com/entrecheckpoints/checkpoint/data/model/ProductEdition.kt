package com.entrecheckpoints.checkpoint.data.model

enum class ProductEdition(val id: String, val displayName: String) {
    BASE("base", "Juego base"),
    DELUXE("deluxe", "Deluxe / Gold"),
    COMPLETE("complete", "Complete / GOTY"),
    REMAKE_REMASTER("remake_remaster", "Remake / Remaster"),
    BUNDLE("bundle", "Bundle"),
    DLC("dlc", "DLC / Expansión"),
    UPGRADE("upgrade", "Mejora / Upgrade"),
    OTHER("other", "Otra edición");

    companion object {
        fun fromId(id: String?): ProductEdition = entries.firstOrNull { it.id == id } ?: BASE
    }
}
