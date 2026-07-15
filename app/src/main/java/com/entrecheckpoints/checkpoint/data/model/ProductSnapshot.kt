package com.entrecheckpoints.checkpoint.data.model

data class ProductSnapshot(
    val store: Store,
    val productId: String?,
    val productType: String? = null,
    val title: String,
    val url: String,
    val imageUrl: String?,
    val priceCents: Long,
    val regularPriceCents: Long = priceCents,
    val currency: String = "MXN",
    val region: String = "MX",
    val discountPercent: Int = 0,
    val offerEndsAt: Long? = null,
    val detectedSubscriptions: Set<SubscriptionService> = emptySet(),
    val source: String,
    val detectedAt: Long = System.currentTimeMillis(),
)
