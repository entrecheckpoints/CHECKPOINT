package com.entrecheckpoints.checkpoint.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.entrecheckpoints.checkpoint.CheckpointApplication
import com.entrecheckpoints.checkpoint.MainActivity
import com.entrecheckpoints.checkpoint.R
import com.entrecheckpoints.checkpoint.data.local.CheckpointDatabase
import com.entrecheckpoints.checkpoint.data.local.GameEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class CheckpointWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val application = context.applicationContext as CheckpointApplication
                    runCatching { application.container.repository.refreshAll() }
                    val manager = AppWidgetManager.getInstance(context)
                    val ids = manager.getAppWidgetIds(android.content.ComponentName(context, CheckpointWidgetProvider::class.java))
                    updateWidgets(context, manager, ids)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateWidgets(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.entrecheckpoints.checkpoint.widget.REFRESH"
        fun updateAll(context: Context, manager: AppWidgetManager, ids: IntArray) {
            CoroutineScope(Dispatchers.IO).launch { updateWidgets(context, manager, ids) }
        }

        private suspend fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val games = CheckpointDatabase.get(context).checkpointDao().getAllGames()
                .filter { it.discountPercent > 0 || (it.targetPriceCents != null && it.priceCents <= it.targetPriceCents) }
                .sortedWith(compareByDescending<GameEntity> { it.discountPercent }.thenBy { it.priceCents })
                .take(3)
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.checkpoint_widget)
                val openIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    9000 + id,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                val refreshIntent = Intent(context, CheckpointWidgetProvider::class.java).setAction(ACTION_REFRESH)
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    12000 + id,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)
                views.setTextViewText(R.id.widget_summary, "${games.size} ofertas destacadas")
                bindRow(views, R.id.widget_row_1, R.id.widget_title_1, R.id.widget_price_1, games.getOrNull(0))
                bindRow(views, R.id.widget_row_2, R.id.widget_title_2, R.id.widget_price_2, games.getOrNull(1))
                bindRow(views, R.id.widget_row_3, R.id.widget_title_3, R.id.widget_price_3, games.getOrNull(2))
                manager.updateAppWidget(id, views)
            }
        }

        private fun bindRow(
            views: RemoteViews,
            rowId: Int,
            titleId: Int,
            priceId: Int,
            game: GameEntity?,
        ) {
            views.setViewVisibility(rowId, if (game == null) View.GONE else View.VISIBLE)
            if (game == null) return
            views.setTextViewText(titleId, game.title)
            views.setTextViewText(priceId, formatPrice(game.priceCents, game.currency))
        }

        private fun formatPrice(cents: Long, currencyCode: String): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            runCatching { formatter.currency = Currency.getInstance(currencyCode) }
            return formatter.format(cents / 100.0)
        }
    }
}
