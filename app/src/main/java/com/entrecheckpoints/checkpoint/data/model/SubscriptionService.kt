package com.entrecheckpoints.checkpoint.data.model

enum class SubscriptionService(val id: String, val displayName: String) {
    GAME_PASS("game_pass", "Game Pass"),
    PS_PLUS("ps_plus", "PlayStation Plus"),
    EA_PLAY("ea_play", "EA Play"),
    UBISOFT_PLUS("ubisoft_plus", "Ubisoft+"),
    NINTENDO_SWITCH_ONLINE("nso", "Nintendo Switch Online");

    companion object {
        fun fromCsv(value: String?): Set<SubscriptionService> = value.orEmpty()
            .split(',')
            .map(String::trim)
            .filter(String::isNotBlank)
            .mapNotNull { id -> entries.firstOrNull { it.id == id } }
            .toSet()

        fun toCsv(values: Set<SubscriptionService>): String = values.joinToString(",") { it.id }
    }
}
