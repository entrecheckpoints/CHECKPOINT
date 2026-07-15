package com.entrecheckpoints.checkpoint.data

import com.entrecheckpoints.checkpoint.data.analytics.TitleNormalizer
import com.entrecheckpoints.checkpoint.data.local.CheckpointDao
import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.GameEventEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.local.SyncRunEntity
import com.entrecheckpoints.checkpoint.data.model.AlertEventType
import com.entrecheckpoints.checkpoint.data.model.AlertRules
import com.entrecheckpoints.checkpoint.data.model.GameFormat
import com.entrecheckpoints.checkpoint.data.model.LibraryStatus
import com.entrecheckpoints.checkpoint.data.model.ProductEdition
import com.entrecheckpoints.checkpoint.data.model.ProductSnapshot
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.data.model.SubscriptionService
import com.entrecheckpoints.checkpoint.data.network.ParserUtils
import com.entrecheckpoints.checkpoint.data.network.StoreRegistry
import com.entrecheckpoints.checkpoint.data.network.WishlistImporter
import com.entrecheckpoints.checkpoint.notifications.NotificationHelper
import com.entrecheckpoints.checkpoint.widget.CheckpointWidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CheckpointRepository(
    private val dao: CheckpointDao,
    private val stores: StoreRegistry,
    private val wishlistImporter: WishlistImporter,
    private val preferences: AppPreferences,
    private val notifications: NotificationHelper,
    private val widgetUpdater: CheckpointWidgetUpdater,
) {
    private val syncMutex = Mutex()

    fun observeGames(): Flow<List<GameEntity>> = dao.observeGames()
    fun observeHistory(gameId: String): Flow<List<PricePointEntity>> = dao.observeHistory(gameId)
    fun observeAllHistory(): Flow<List<PricePointEntity>> = dao.observeAllHistory()
    fun observeEvents(): Flow<List<GameEventEntity>> = dao.observeEvents()
    fun observeSyncRuns(): Flow<List<SyncRunEntity>> = dao.observeSyncRuns()

    suspend fun addFromUrl(url: String, targetPriceCents: Long?): GameEntity = syncMutex.withLock {
        addInternal(url.trim(), targetPriceCents)
    }

    suspend fun importFromText(raw: String): BulkImportResult = syncMutex.withLock {
        val resolved = wishlistImporter.resolve(raw)
        var added = 0
        var updated = 0
        val failures = mutableListOf<String>()
        resolved.productUrls.forEach { url ->
            runCatching {
                val beforeCount = dao.getAllGames().size
                addInternal(url, null)
                if (dao.getAllGames().size > beforeCount) added += 1 else updated += 1
            }.onFailure { failures += (it.message ?: url) }
        }
        BulkImportResult(
            added = added,
            updated = updated,
            failed = failures.size,
            ignored = resolved.ignoredLines,
            sourceLists = resolved.sourceCount,
            errors = failures.take(5),
        )
    }

    suspend fun refreshGame(id: String): GameEntity = syncMutex.withLock {
        val before = dao.getGame(id) ?: error("El juego ya no está en seguimiento.")
        refreshInternal(before)
    }

    suspend fun refreshAll(): SyncResult = syncMutex.withLock {
        val started = System.currentTimeMillis()
        val games = dao.getAllGames()
        var updated = 0
        val errors = mutableListOf<String>()
        games.forEach { before ->
            runCatching { refreshInternal(before) }
                .onSuccess { updated += 1 }
                .onFailure { errors += "${before.title}: ${it.message ?: "error"}" }
        }
        val finished = System.currentTimeMillis()
        val result = SyncResult(
            updated = updated,
            errors = errors.size,
            startedAt = started,
            finishedAt = finished,
        )
        dao.insertSyncRun(
            SyncRunEntity(
                startedAt = started,
                finishedAt = finished,
                updated = updated,
                errors = errors.size,
                status = when {
                    errors.isEmpty() -> "ok"
                    updated > 0 -> "partial"
                    else -> "error"
                },
                errorSummary = errors.take(4).joinToString("\n").takeIf(String::isNotBlank),
            ),
        )
        dao.trimSyncRuns(60)
        widgetUpdater.requestUpdate()
        result
    }

    suspend fun deleteGame(id: String) {
        dao.deleteGame(id)
        widgetUpdater.requestUpdate()
    }

    suspend fun setTarget(id: String, targetCents: Long?) {
        dao.updateTarget(id, targetCents)
        widgetUpdater.requestUpdate()
    }

    suspend fun updateAlerts(id: String, rules: AlertRules) = updateGame(id) { game ->
        game.copy(
            alertAnyDrop = rules.anyDrop,
            alertTarget = rules.targetReached,
            alertDiscountPercent = rules.discountPercent?.coerceIn(1, 100),
            alertDropAmountCents = rules.dropAmountCents?.coerceAtLeast(0),
            alertNewLow = rules.newLow,
            alertOfferEndingSoon = rules.offerEndingSoon,
        )
    }

    suspend fun updateComparison(id: String, key: String, edition: ProductEdition) = updateGame(id) { game ->
        game.copy(
            comparisonKey = key.trim().ifBlank { TitleNormalizer.comparisonKey(game.title) },
            editionLabel = edition.id,
        )
    }

    suspend fun updateLibrary(
        id: String,
        status: LibraryStatus,
        paidPriceCents: Long?,
        purchaseDate: Long?,
        format: GameFormat,
        rating: Int?,
    ) = updateGame(id) { game ->
        game.copy(
            libraryStatus = status.id,
            ownedStoreId = if (status == LibraryStatus.WISHLIST) null else game.storeId,
            paidPriceCents = if (status == LibraryStatus.WISHLIST) null else paidPriceCents,
            purchaseDate = if (status == LibraryStatus.WISHLIST) null else purchaseDate,
            gameFormat = format.id,
            personalRating = if (status == LibraryStatus.WISHLIST) null else rating?.coerceIn(1, 10),
        )
    }

    suspend fun updateSubscriptions(id: String, services: Set<SubscriptionService>) = updateGame(id) { game ->
        game.copy(subscriptionTags = SubscriptionService.toCsv(services))
    }

    suspend fun updateNotes(id: String, notes: String) = updateGame(id) { game ->
        game.copy(notes = notes.trim())
    }

    suspend fun markEventsSeen() = dao.markAllEventsSeen()

    suspend fun allForBackup(): BackupPayload = BackupPayload(
        games = dao.getAllGames(),
        points = dao.getAllHistory(),
        events = dao.getAllEvents(),
    )

    suspend fun replaceAll(
        games: List<GameEntity>,
        points: List<PricePointEntity>,
        events: List<GameEventEntity>,
    ) {
        syncMutex.withLock { dao.replaceEverything(games, points, events) }
        widgetUpdater.requestUpdate()
    }

    private suspend fun addInternal(url: String, targetPriceCents: Long?): GameEntity {
        val snapshot = stores.fetch(url)
        val existing = snapshot.productId?.let { dao.findByProduct(snapshot.store.id, it) }
            ?: dao.findByUrl(snapshot.url)
        val now = System.currentTimeMillis()
        val game = snapshot.toEntity(
            id = existing?.id ?: gameId(snapshot),
            existing = existing,
            targetPriceCents = targetPriceCents ?: existing?.targetPriceCents,
            now = now,
        )
        dao.upsertGame(game)
        appendHistoryIfNeeded(existing, game, snapshot.source, now)
        widgetUpdater.requestUpdate()
        return game
    }

    private suspend fun refreshInternal(before: GameEntity): GameEntity {
        try {
            val snapshot = stores.byStore(Store.fromId(before.storeId)).fetch(before.url)
            val now = System.currentTimeMillis()
            val history = dao.getHistory(before.id)
            val after = snapshot.toEntity(
                id = before.id,
                existing = before,
                targetPriceCents = before.targetPriceCents,
                now = now,
            )
            val batch = buildEvents(before, after, history, now)
            dao.upsertGame(after)
            appendHistoryIfNeeded(before, after, snapshot.source, now)
            if (batch.all.isNotEmpty()) {
                dao.insertEvents(batch.all)
                dao.trimEvents(500)
            }
            notifications.notifyEvents(after, batch.notify)
            widgetUpdater.requestUpdate()
            return after
        } catch (error: Exception) {
            val now = System.currentTimeMillis()
            val failed = before.copy(
                lastChecked = now,
                lastStatus = "error",
                lastError = error.message ?: "Error desconocido.",
            )
            dao.upsertGame(failed)
            if (!dao.hasRecentEvent(before.id, AlertEventType.SOURCE_ERROR.id, now - DAY)) {
                dao.insertEvents(
                    listOf(
                        event(
                            game = failed,
                            type = AlertEventType.SOURCE_ERROR,
                            oldPrice = before.priceCents,
                            newPrice = before.priceCents,
                            now = now,
                            detail = error.message ?: "La tienda no devolvió datos legibles.",
                        ),
                    ),
                )
            }
            throw error
        }
    }

    private suspend fun updateGame(id: String, transform: (GameEntity) -> GameEntity) {
        val game = dao.getGame(id) ?: error("El juego ya no existe.")
        dao.upsertGame(transform(game))
        widgetUpdater.requestUpdate()
    }

    private suspend fun buildEvents(
        before: GameEntity,
        after: GameEntity,
        history: List<PricePointEntity>,
        now: Long,
    ): EventBatch {
        val all = mutableListOf<GameEventEntity>()
        val notify = mutableListOf<GameEventEntity>()
        val drop = before.priceCents - after.priceCents
        val priceDropped = drop > 0

        if (priceDropped) {
            val item = event(
                after,
                AlertEventType.PRICE_DROP,
                before.priceCents,
                after.priceCents,
                now,
                "Bajó de ${notifications.price(before.priceCents, after.currency)} a ${notifications.price(after.priceCents, after.currency)}.",
            )
            all += item
            if (after.alertAnyDrop) notify += item
        }
        if (after.priceCents < before.minPriceCents) {
            val item = event(
                after,
                AlertEventType.NEW_LOW,
                before.minPriceCents,
                after.priceCents,
                now,
                "Nuevo mínimo registrado: ${notifications.price(after.priceCents, after.currency)}.",
            )
            all += item
            if (after.alertNewLow) notify += item
        }
        val target = after.targetPriceCents
        if (target != null && after.priceCents <= target && before.priceCents > target) {
            val item = event(
                after,
                AlertEventType.TARGET_REACHED,
                before.priceCents,
                after.priceCents,
                now,
                "Llegó a tu objetivo de ${notifications.price(target, after.currency)}.",
            )
            all += item
            if (after.alertTarget) notify += item
        }
        val threshold = after.alertDiscountPercent
        if (threshold != null && after.discountPercent >= threshold && before.discountPercent < threshold) {
            val item = event(
                after,
                AlertEventType.DISCOUNT_THRESHOLD,
                before.priceCents,
                after.priceCents,
                now,
                "Alcanzó ${after.discountPercent}% de descuento; tu regla era $threshold%.",
            )
            all += item
            notify += item
        }
        val amount = after.alertDropAmountCents
        if (amount != null && priceDropped && drop >= amount) {
            val item = event(
                after,
                AlertEventType.DROP_AMOUNT,
                before.priceCents,
                after.priceCents,
                now,
                "Bajó ${notifications.price(drop, after.currency)}, superando tu regla.",
            )
            all += item
            notify += item
        }
        if (before.discountPercent == 0 && after.discountPercent > 0 && history.any { it.discountPercent > 0 }) {
            val item = event(
                after,
                AlertEventType.OFFER_RETURNED,
                before.priceCents,
                after.priceCents,
                now,
                "Regresó una oferta detectada anteriormente: ${after.discountPercent}% de descuento.",
            )
            all += item
            if (after.alertAnyDrop) notify += item
        }
        val ending = after.offerEndsAt
        if (
            ending != null && ending in (now + HOUR)..(now + 48 * HOUR) &&
            !dao.hasRecentEvent(after.id, AlertEventType.OFFER_ENDING.id, now - DAY)
        ) {
            val hours = ((ending - now) / HOUR).coerceAtLeast(1)
            val item = event(
                after,
                AlertEventType.OFFER_ENDING,
                after.priceCents,
                after.priceCents,
                now,
                "La oferta podría terminar en aproximadamente $hours horas.",
            )
            all += item
            if (after.alertOfferEndingSoon) notify += item
        }
        return EventBatch(all.distinctBy { it.type }, notify.distinctBy { it.type })
    }

    private fun event(
        game: GameEntity,
        type: AlertEventType,
        oldPrice: Long?,
        newPrice: Long?,
        now: Long,
        detail: String,
    ): GameEventEntity = GameEventEntity(
        gameId = game.id,
        type = type.id,
        title = game.title,
        oldPriceCents = oldPrice,
        newPriceCents = newPrice,
        discountPercent = game.discountPercent,
        currency = game.currency,
        createdAt = now,
        detail = detail,
    )

    private suspend fun appendHistoryIfNeeded(
        before: GameEntity?,
        after: GameEntity,
        source: String,
        now: Long,
    ) {
        val latest = dao.getHistory(after.id).lastOrNull()
        val changed = latest == null || latest.priceCents != after.priceCents || latest.regularPriceCents != after.regularPriceCents
        val stale = latest == null || now - latest.checkedAt >= DAY
        if (before == null || changed || stale) {
            dao.insertHistory(
                PricePointEntity(
                    gameId = after.id,
                    priceCents = after.priceCents,
                    regularPriceCents = after.regularPriceCents,
                    discountPercent = after.discountPercent,
                    checkedAt = now,
                    source = source,
                ),
            )
            dao.trimHistory(after.id, preferences.maxHistory)
        }
    }

    private fun ProductSnapshot.toEntity(
        id: String,
        existing: GameEntity?,
        targetPriceCents: Long?,
        now: Long,
    ): GameEntity {
        val detected = detectedSubscriptions
        val existingSubscriptions = SubscriptionService.fromCsv(existing?.subscriptionTags)
        return GameEntity(
            id = id,
            storeId = store.id,
            productId = productId,
            productType = productType,
            title = title,
            url = url,
            imageUrl = imageUrl ?: existing?.imageUrl,
            priceCents = priceCents,
            regularPriceCents = regularPriceCents,
            currency = currency,
            region = region,
            discountPercent = discountPercent,
            minPriceCents = minOf(existing?.minPriceCents ?: priceCents, priceCents),
            targetPriceCents = targetPriceCents,
            addedAt = existing?.addedAt ?: now,
            lastChecked = now,
            lastStatus = "ok",
            lastError = null,
            source = source,
            comparisonKey = existing?.comparisonKey?.takeIf(String::isNotBlank) ?: TitleNormalizer.comparisonKey(title),
            editionLabel = existing?.editionLabel ?: TitleNormalizer.edition(title, productType).id,
            libraryStatus = existing?.libraryStatus ?: LibraryStatus.WISHLIST.id,
            ownedStoreId = existing?.ownedStoreId,
            paidPriceCents = existing?.paidPriceCents,
            purchaseDate = existing?.purchaseDate,
            gameFormat = existing?.gameFormat ?: GameFormat.DIGITAL.id,
            personalRating = existing?.personalRating,
            subscriptionTags = SubscriptionService.toCsv(existingSubscriptions + detected),
            notes = existing?.notes.orEmpty(),
            offerEndsAt = offerEndsAt,
            alertAnyDrop = existing?.alertAnyDrop ?: true,
            alertTarget = existing?.alertTarget ?: true,
            alertDiscountPercent = existing?.alertDiscountPercent,
            alertDropAmountCents = existing?.alertDropAmountCents,
            alertNewLow = existing?.alertNewLow ?: true,
            alertOfferEndingSoon = existing?.alertOfferEndingSoon ?: false,
            lastNotifiedPriceCents = existing?.lastNotifiedPriceCents,
            lastNotifiedAt = existing?.lastNotifiedAt,
        )
    }

    private fun gameId(snapshot: ProductSnapshot): String = snapshot.productId
        ?.let { "${snapshot.store.id}-${it.lowercase()}" }
        ?: "${snapshot.store.id}-url-${ParserUtils.idFrom(snapshot.url)}"

    private data class EventBatch(
        val all: List<GameEventEntity>,
        val notify: List<GameEventEntity>,
    )

    companion object {
        private const val HOUR = 60L * 60L * 1000L
        private const val DAY = 24L * HOUR
    }
}

data class SyncResult(
    val updated: Int,
    val errors: Int,
    val startedAt: Long,
    val finishedAt: Long,
)

data class BulkImportResult(
    val added: Int,
    val updated: Int,
    val failed: Int,
    val ignored: Int,
    val sourceLists: Int,
    val errors: List<String>,
)

data class BackupPayload(
    val games: List<GameEntity>,
    val points: List<PricePointEntity>,
    val events: List<GameEventEntity>,
)
