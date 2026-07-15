package com.entrecheckpoints.checkpoint.data.network

import com.entrecheckpoints.checkpoint.data.model.ProductSnapshot
import com.entrecheckpoints.checkpoint.data.model.Store

class StoreRegistry(http: HttpClient) {
    private val parsers: List<StoreParser> = listOf(
        NintendoParser(http),
        SteamParser(http),
        XboxParser(http),
    )

    fun detect(url: String): StoreParser? = parsers.firstOrNull { it.matches(url) }

    fun byStore(store: Store): StoreParser = parsers.first { it.store == store }

    suspend fun fetch(url: String): ProductSnapshot =
        detect(url)?.fetch(url) ?: throw StoreFetchException(
            "Checkpoint reconoce enlaces de Nintendo eShop, Steam y Xbox Store.",
        )
}
