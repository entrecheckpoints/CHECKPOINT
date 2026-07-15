package com.entrecheckpoints.checkpoint.data.network

import com.entrecheckpoints.checkpoint.data.model.ProductSnapshot
import com.entrecheckpoints.checkpoint.data.model.Store

interface StoreParser {
    val store: Store
    fun matches(url: String): Boolean
    suspend fun fetch(url: String): ProductSnapshot
}
