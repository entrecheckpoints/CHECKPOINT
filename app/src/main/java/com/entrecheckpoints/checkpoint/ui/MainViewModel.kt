package com.entrecheckpoints.checkpoint.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.entrecheckpoints.checkpoint.AppContainer
import com.entrecheckpoints.checkpoint.data.BackupCodec
import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.GameEventEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.local.SyncRunEntity
import com.entrecheckpoints.checkpoint.data.model.AlertRules
import com.entrecheckpoints.checkpoint.data.model.GameFormat
import com.entrecheckpoints.checkpoint.data.model.LibraryStatus
import com.entrecheckpoints.checkpoint.data.model.ProductEdition
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.data.model.SubscriptionService
import com.entrecheckpoints.checkpoint.data.network.ParserUtils
import com.entrecheckpoints.checkpoint.ui.model.AppSection
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val container: AppContainer,
    private val scheduleSync: (Long) -> Unit,
) : ViewModel() {
    val games: StateFlow<List<GameEntity>> = container.repository.observeGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allHistory: StateFlow<List<PricePointEntity>> = container.repository.observeAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val events: StateFlow<List<GameEventEntity>> = container.repository.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val syncRuns: StateFlow<List<SyncRunEntity>> = container.repository.observeSyncRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedGameId = MutableStateFlow<String?>(null)
    val selectedHistory: StateFlow<List<PricePointEntity>> = selectedGameId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else container.repository.observeHistory(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val section = MutableStateFlow(AppSection.TRACKING)
    val query = MutableStateFlow("")
    val storeFilter = MutableStateFlow<Store?>(null)
    val libraryFilter = MutableStateFlow<LibraryStatus?>(null)
    val isBusy = MutableStateFlow(false)
    val pendingAddUrl = MutableStateFlow<String?>(null)
    val intervalHours = MutableStateFlow(container.preferences.intervalHours)
    val notificationsEnabled = MutableStateFlow(container.preferences.notificationsEnabled)
    val themeMode = MutableStateFlow(container.preferences.themeMode)
    val monthlyBudgetCents = MutableStateFlow(container.preferences.monthlyBudgetCents)
    val activeSubscriptions = MutableStateFlow(container.preferences.activeSubscriptions)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    fun handleSharedText(text: String?) {
        val url = text?.let { Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE).find(it)?.value }
        if (url != null) pendingAddUrl.value = url.trimEnd('.', ',', ')', ']')
    }

    fun consumePendingUrl() {
        pendingAddUrl.value = null
    }

    fun navigate(value: AppSection) {
        section.value = value
        if (value == AppSection.DEALS) markEventsSeen()
    }

    fun selectGame(id: String?) {
        selectedGameId.value = id
    }

    fun addGame(url: String, targetText: String, onDone: () -> Unit) = launchBusy {
        val target = targetText.takeIf(String::isNotBlank)?.let(ParserUtils::parseMoneyToCents)
        if (targetText.isNotBlank() && target == null) error("El precio objetivo no tiene un formato válido.")
        val game = container.repository.addFromUrl(url, target)
        _messages.emit("${game.title} quedó bajo vigilancia.")
        onDone()
    }

    fun importWishlist(raw: String, onDone: () -> Unit = {}) = launchBusy {
        val result = container.repository.importFromText(raw)
        val suffix = buildString {
            if (result.failed > 0) append(" · ${result.failed} fallaron")
            if (result.ignored > 0) append(" · ${result.ignored} líneas ignoradas")
        }
        _messages.emit("Importación: ${result.added} nuevos, ${result.updated} actualizados$suffix.")
        onDone()
    }

    fun refreshGame(id: String) = launchBusy {
        val game = container.repository.refreshGame(id)
        _messages.emit("${game.title}: precio actualizado.")
    }

    fun refreshAll() = launchBusy {
        val result = container.repository.refreshAll()
        _messages.emit("Sincronización: ${result.updated} actualizados, ${result.errors} con error.")
    }

    fun deleteGame(id: String, onDone: () -> Unit = {}) = launchBusy {
        container.repository.deleteGame(id)
        if (selectedGameId.value == id) selectedGameId.value = null
        _messages.emit("Juego eliminado del seguimiento.")
        onDone()
    }

    fun setTarget(id: String, text: String, onDone: () -> Unit = {}) = launchBusy {
        val target = text.takeIf(String::isNotBlank)?.let(ParserUtils::parseMoneyToCents)
        if (text.isNotBlank() && target == null) error("El precio objetivo no tiene un formato válido.")
        container.repository.setTarget(id, target)
        _messages.emit(if (target == null) "Objetivo eliminado." else "Objetivo actualizado.")
        onDone()
    }

    fun updateAlerts(
        id: String,
        anyDrop: Boolean,
        targetReached: Boolean,
        discountText: String,
        dropAmountText: String,
        newLow: Boolean,
        offerEnding: Boolean,
        onDone: () -> Unit = {},
    ) = launchBusy {
        val discount = discountText.trim().takeIf(String::isNotBlank)?.toIntOrNull()
        if (discountText.isNotBlank() && discount == null) error("El porcentaje de descuento no es válido.")
        val amount = dropAmountText.trim().takeIf(String::isNotBlank)?.let(ParserUtils::parseMoneyToCents)
        if (dropAmountText.isNotBlank() && amount == null) error("La cantidad de bajada no es válida.")
        container.repository.updateAlerts(
            id,
            AlertRules(anyDrop, targetReached, discount, amount, newLow, offerEnding),
        )
        _messages.emit("Reglas de alerta actualizadas.")
        onDone()
    }

    fun updateComparison(id: String, key: String, edition: ProductEdition) = launchBusy {
        container.repository.updateComparison(id, key, edition)
        _messages.emit("Comparación actualizada.")
    }

    fun updateLibrary(
        id: String,
        status: LibraryStatus,
        paidText: String,
        purchaseDate: Long?,
        format: GameFormat,
        ratingText: String,
        onDone: () -> Unit = {},
    ) = launchBusy {
        val paid = paidText.trim().takeIf(String::isNotBlank)?.let(ParserUtils::parseMoneyToCents)
        if (paidText.isNotBlank() && paid == null) error("El precio pagado no es válido.")
        val rating = ratingText.trim().takeIf(String::isNotBlank)?.toIntOrNull()
        if (ratingText.isNotBlank() && rating !in 1..10) error("La calificación debe estar entre 1 y 10.")
        container.repository.updateLibrary(id, status, paid, purchaseDate, format, rating)
        _messages.emit("Biblioteca actualizada.")
        onDone()
    }

    fun updateSubscriptions(id: String, services: Set<SubscriptionService>) = launchBusy {
        container.repository.updateSubscriptions(id, services)
        _messages.emit("Suscripciones del juego actualizadas.")
    }

    fun updateNotes(id: String, notes: String) = launchBusy {
        container.repository.updateNotes(id, notes)
        _messages.emit("Notas guardadas.")
    }

    fun updateInterval(hours: Long) {
        val safe = hours.coerceAtLeast(1L)
        container.preferences.intervalHours = safe
        intervalHours.value = safe
        scheduleSync(safe)
    }

    fun updateTheme(mode: CheckpointThemeMode) {
        container.preferences.themeMode = mode
        themeMode.value = mode
    }

    fun updateNotifications(enabled: Boolean) {
        container.preferences.notificationsEnabled = enabled
        notificationsEnabled.value = enabled
    }

    fun updateBudget(text: String) {
        val value = text.trim().takeIf(String::isNotBlank)?.let(ParserUtils::parseMoneyToCents) ?: 0L
        container.preferences.monthlyBudgetCents = value
        monthlyBudgetCents.value = value
    }

    fun toggleActiveSubscription(service: SubscriptionService, enabled: Boolean) {
        val updated = activeSubscriptions.value.toMutableSet().apply {
            if (enabled) add(service) else remove(service)
        }.toSet()
        container.preferences.activeSubscriptions = updated
        activeSubscriptions.value = updated
    }

    fun markEventsSeen() {
        viewModelScope.launch { container.repository.markEventsSeen() }
    }

    fun exportJson(onReady: (String) -> Unit) = launchBusy {
        val payload = container.repository.allForBackup()
        onReady(BackupCodec.encode(payload.games, payload.points, payload.events, container.preferences))
    }

    fun importJson(raw: String, onDone: () -> Unit = {}) = launchBusy {
        val imported = BackupCodec.decode(raw)
        container.repository.replaceAll(imported.games, imported.points, imported.events)
        imported.intervalHours?.let(::updateInterval)
        imported.notificationsEnabled?.let(::updateNotifications)
        imported.themeMode?.let(::updateTheme)
        imported.monthlyBudgetCents?.let {
            container.preferences.monthlyBudgetCents = it
            monthlyBudgetCents.value = it
        }
        imported.activeSubscriptions?.let {
            container.preferences.activeSubscriptions = it
            activeSubscriptions.value = it
        }
        _messages.emit("Se importaron ${imported.games.size} juegos y ${imported.events.size} eventos.")
        onDone()
    }

    private fun launchBusy(block: suspend () -> Unit) {
        viewModelScope.launch {
            isBusy.value = true
            runCatching { block() }
                .onFailure { _messages.emit(it.message ?: "Algo salió mal.") }
            isBusy.value = false
        }
    }

    class Factory(
        private val container: AppContainer,
        private val scheduleSync: (Long) -> Unit,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(container, scheduleSync) as T
    }
}
