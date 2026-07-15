package com.entrecheckpoints.checkpoint.data

import android.content.Context
import com.entrecheckpoints.checkpoint.data.model.SubscriptionService
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointThemeMode

class AppPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("checkpoint_preferences", Context.MODE_PRIVATE)

    var intervalHours: Long
        get() = preferences.getLong(KEY_INTERVAL_HOURS, 12L).coerceAtLeast(1L)
        set(value) = preferences.edit().putLong(KEY_INTERVAL_HOURS, value.coerceAtLeast(1L)).apply()

    var notificationsEnabled: Boolean
        get() = preferences.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = preferences.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var themeMode: CheckpointThemeMode
        get() = CheckpointThemeMode.fromId(preferences.getString(KEY_THEME_MODE, null))
        set(value) = preferences.edit().putString(KEY_THEME_MODE, value.id).apply()

    var maxHistory: Int
        get() = preferences.getInt(KEY_MAX_HISTORY, 365).coerceIn(30, 2_000)
        set(value) = preferences.edit().putInt(KEY_MAX_HISTORY, value.coerceIn(30, 2_000)).apply()

    var monthlyBudgetCents: Long
        get() = preferences.getLong(KEY_MONTHLY_BUDGET, 0L).coerceAtLeast(0L)
        set(value) = preferences.edit().putLong(KEY_MONTHLY_BUDGET, value.coerceAtLeast(0L)).apply()

    var activeSubscriptions: Set<SubscriptionService>
        get() = SubscriptionService.fromCsv(preferences.getString(KEY_ACTIVE_SUBSCRIPTIONS, ""))
        set(value) = preferences.edit()
            .putString(KEY_ACTIVE_SUBSCRIPTIONS, SubscriptionService.toCsv(value))
            .apply()

    companion object {
        private const val KEY_INTERVAL_HOURS = "interval_hours"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_MAX_HISTORY = "max_history"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget_cents"
        private const val KEY_ACTIVE_SUBSCRIPTIONS = "active_subscriptions"
    }
}
