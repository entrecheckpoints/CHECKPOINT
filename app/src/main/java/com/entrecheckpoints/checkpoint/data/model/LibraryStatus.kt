package com.entrecheckpoints.checkpoint.data.model

enum class LibraryStatus(val id: String, val displayName: String) {
    WISHLIST("wishlist", "Deseado"),
    OWNED("owned", "Comprado"),
    PLAYING("playing", "Jugando"),
    COMPLETED("completed", "Terminado"),
    DROPPED("dropped", "Abandonado con honor");

    companion object {
        fun fromId(id: String?): LibraryStatus = entries.firstOrNull { it.id == id } ?: WISHLIST
    }
}
