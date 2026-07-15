package com.entrecheckpoints.checkpoint.data.model

data class AlertRules(
    val anyDrop: Boolean = true,
    val targetReached: Boolean = true,
    val discountPercent: Int? = null,
    val dropAmountCents: Long? = null,
    val newLow: Boolean = true,
    val offerEndingSoon: Boolean = false,
)
