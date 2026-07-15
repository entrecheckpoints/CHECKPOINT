package com.entrecheckpoints.checkpoint.ui

import java.text.DateFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

fun formatPrice(cents: Long, currencyCode: String): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    runCatching { formatter.currency = Currency.getInstance(currencyCode) }
    return formatter.format(cents / 100.0)
}

fun formatDate(timestamp: Long): String = DateFormat.getDateTimeInstance(
    DateFormat.MEDIUM,
    DateFormat.SHORT,
    Locale("es", "MX"),
).format(Date(timestamp))

fun formatRelative(timestamp: Long): String {
    val minutes = ((System.currentTimeMillis() - timestamp).coerceAtLeast(0L) / 60_000L)
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> "hace ${minutes}m"
        minutes < 1_440 -> "hace ${minutes / 60}h"
        else -> "hace ${minutes / 1_440}d"
    }
}
