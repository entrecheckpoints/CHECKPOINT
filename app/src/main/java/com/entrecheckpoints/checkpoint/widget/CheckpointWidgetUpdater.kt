package com.entrecheckpoints.checkpoint.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

class CheckpointWidgetUpdater(private val context: Context) {
    fun requestUpdate() {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, CheckpointWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isNotEmpty()) CheckpointWidgetProvider.updateAll(context, manager, ids)
    }
}
