package com.entrecheckpoints.checkpoint.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.entrecheckpoints.checkpoint.MainActivity
import com.entrecheckpoints.checkpoint.R
import com.entrecheckpoints.checkpoint.data.AppPreferences
import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.GameEventEntity
import com.entrecheckpoints.checkpoint.data.model.AlertEventType
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class NotificationHelper(
    private val context: Context,
    private val preferences: AppPreferences,
) {
    init {
        createChannel()
    }

    fun notifyEvents(game: GameEntity, events: List<GameEventEntity>) {
        if (!preferences.notificationsEnabled || !canNotify() || events.isEmpty()) return
        val event = events.maxByOrNull { priority(AlertEventType.fromId(it.type)) } ?: return
        val type = AlertEventType.fromId(event.type)
        val title = when (type) {
            AlertEventType.NEW_LOW -> "${game.title}: nuevo mínimo"
            AlertEventType.TARGET_REACHED -> "${game.title}: objetivo alcanzado"
            AlertEventType.DISCOUNT_THRESHOLD -> "${game.title}: descuento objetivo"
            AlertEventType.DROP_AMOUNT -> "${game.title}: bajada importante"
            AlertEventType.OFFER_RETURNED -> "${game.title}: la oferta volvió"
            AlertEventType.OFFER_ENDING -> "${game.title}: oferta por terminar"
            AlertEventType.SOURCE_ERROR -> "Checkpoint no pudo revisar ${game.title}"
            AlertEventType.PRICE_DROP -> "${game.title} bajó de precio"
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_GAME_ID, game.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            game.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PRICE_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(event.detail)
            .setStyle(NotificationCompat.BigTextStyle().bigText(event.detail))
            .setSubText("Checkpoint · ${game.storeId}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(game.id.hashCode(), notification)
    }

    fun price(cents: Long?, currency: String): String {
        if (cents == null) return "—"
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply {
            runCatching { Currency.getInstance(currency) }.getOrNull()?.let { this.currency = it }
        }
        return formatter.format(cents / 100.0)
    }

    private fun priority(type: AlertEventType): Int = when (type) {
        AlertEventType.TARGET_REACHED -> 100
        AlertEventType.NEW_LOW -> 90
        AlertEventType.DISCOUNT_THRESHOLD -> 80
        AlertEventType.DROP_AMOUNT -> 75
        AlertEventType.OFFER_ENDING -> 70
        AlertEventType.OFFER_RETURNED -> 65
        AlertEventType.PRICE_DROP -> 60
        AlertEventType.SOURCE_ERROR -> 20
    }

    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PRICE_ALERTS,
                "Alertas inteligentes",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Bajadas, mínimos, objetivos, descuentos y ofertas próximas a terminar."
            },
        )
    }

    companion object {
        const val CHANNEL_PRICE_ALERTS = "checkpoint_price_alerts"
    }
}
