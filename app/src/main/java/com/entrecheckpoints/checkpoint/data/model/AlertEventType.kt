package com.entrecheckpoints.checkpoint.data.model

enum class AlertEventType(val id: String, val displayName: String) {
    PRICE_DROP("price_drop", "Bajada de precio"),
    NEW_LOW("new_low", "Nuevo mínimo"),
    TARGET_REACHED("target_reached", "Objetivo alcanzado"),
    DISCOUNT_THRESHOLD("discount_threshold", "Descuento objetivo"),
    DROP_AMOUNT("drop_amount", "Bajada importante"),
    OFFER_RETURNED("offer_returned", "Oferta de regreso"),
    OFFER_ENDING("offer_ending", "Oferta por terminar"),
    SOURCE_ERROR("source_error", "Error de fuente");

    companion object {
        fun fromId(id: String?): AlertEventType = entries.firstOrNull { it.id == id } ?: PRICE_DROP
    }
}
