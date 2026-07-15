package com.entrecheckpoints.checkpoint.data

import com.entrecheckpoints.checkpoint.data.analytics.TitleNormalizer
import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.GameEventEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.model.GameFormat
import com.entrecheckpoints.checkpoint.data.model.LibraryStatus
import com.entrecheckpoints.checkpoint.data.model.ProductEdition
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.data.model.SubscriptionService
import com.entrecheckpoints.checkpoint.data.network.ParserUtils
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointThemeMode
import org.json.JSONArray
import org.json.JSONObject

object BackupCodec {
    data class ImportedData(
        val games: List<GameEntity>,
        val points: List<PricePointEntity>,
        val events: List<GameEventEntity>,
        val intervalHours: Long?,
        val notificationsEnabled: Boolean?,
        val themeMode: CheckpointThemeMode?,
        val monthlyBudgetCents: Long?,
        val activeSubscriptions: Set<SubscriptionService>?,
    )

    fun encode(
        games: List<GameEntity>,
        points: List<PricePointEntity>,
        events: List<GameEventEntity>,
        preferences: AppPreferences,
    ): String {
        val historyByGame = points.groupBy { it.gameId }
        val root = JSONObject()
            .put("version", 5)
            .put("brand", "Checkpoint by Entre Checkpoints")
            .put("exportedAt", System.currentTimeMillis())
            .put(
                "settings",
                JSONObject()
                    .put("intervalHours", preferences.intervalHours)
                    .put("notifications", preferences.notificationsEnabled)
                    .put("maxHistory", preferences.maxHistory)
                    .put("theme", preferences.themeMode.id)
                    .put("monthlyBudget", preferences.monthlyBudgetCents / 100.0)
                    .put("activeSubscriptions", JSONArray(preferences.activeSubscriptions.map { it.id })),
            )

        val gameArray = JSONArray()
        games.forEach { game ->
            val item = JSONObject()
                .put("id", game.id)
                .put("store", game.storeId)
                .put("storeName", Store.fromId(game.storeId).displayName)
                .putNullable("productId", game.productId)
                .putNullable("productType", game.productType)
                .put("title", game.title)
                .put("url", game.url)
                .putNullable("image", game.imageUrl)
                .put("price", game.priceCents / 100.0)
                .put("regularPrice", game.regularPriceCents / 100.0)
                .put("currency", game.currency)
                .put("region", game.region)
                .put("discountPercent", game.discountPercent)
                .put("minPrice", game.minPriceCents / 100.0)
                .putNullable("targetPrice", game.targetPriceCents?.div(100.0))
                .put("addedAt", game.addedAt)
                .put("lastChecked", game.lastChecked)
                .put("lastStatus", game.lastStatus)
                .putNullable("lastError", game.lastError)
                .put("source", game.source)
                .put("comparisonKey", game.comparisonKey)
                .put("edition", game.editionLabel)
                .put("libraryStatus", game.libraryStatus)
                .putNullable("ownedStore", game.ownedStoreId)
                .putNullable("paidPrice", game.paidPriceCents?.div(100.0))
                .putNullable("purchaseDate", game.purchaseDate)
                .put("format", game.gameFormat)
                .putNullable("rating", game.personalRating)
                .put("subscriptions", JSONArray(SubscriptionService.fromCsv(game.subscriptionTags).map { it.id }))
                .put("notes", game.notes)
                .putNullable("offerEndsAt", game.offerEndsAt)
                .put(
                    "alerts",
                    JSONObject()
                        .put("anyDrop", game.alertAnyDrop)
                        .put("target", game.alertTarget)
                        .putNullable("discountPercent", game.alertDiscountPercent)
                        .putNullable("dropAmount", game.alertDropAmountCents?.div(100.0))
                        .put("newLow", game.alertNewLow)
                        .put("offerEnding", game.alertOfferEndingSoon),
                )
            val history = JSONArray()
            historyByGame[game.id].orEmpty().sortedBy { it.checkedAt }.forEach { point ->
                history.put(
                    JSONObject()
                        .put("price", point.priceCents / 100.0)
                        .put("regularPrice", point.regularPriceCents / 100.0)
                        .put("discountPercent", point.discountPercent)
                        .put("checkedAt", point.checkedAt)
                        .put("source", point.source),
                )
            }
            item.put("history", history)
            gameArray.put(item)
        }
        root.put("games", gameArray)

        val eventArray = JSONArray()
        events.sortedBy { it.createdAt }.forEach { event ->
            eventArray.put(
                JSONObject()
                    .put("gameId", event.gameId)
                    .put("type", event.type)
                    .put("title", event.title)
                    .putNullable("oldPrice", event.oldPriceCents?.div(100.0))
                    .putNullable("newPrice", event.newPriceCents?.div(100.0))
                    .put("discountPercent", event.discountPercent)
                    .put("currency", event.currency)
                    .put("createdAt", event.createdAt)
                    .put("seen", event.seen)
                    .put("detail", event.detail),
            )
        }
        root.put("events", eventArray)
        return root.toString(2)
    }

    fun decode(raw: String): ImportedData {
        val parsed = JSONObject(raw)
        val root = parsed.optJSONObject("checkpointState") ?: parsed
        val settings = root.optJSONObject("settings")
        val gamesJson = root.optJSONArray("games")
            ?: throw IllegalArgumentException("El archivo no contiene una lista de juegos.")

        val games = mutableListOf<GameEntity>()
        val points = mutableListOf<PricePointEntity>()
        val events = mutableListOf<GameEventEntity>()
        val now = System.currentTimeMillis()

        for (index in 0 until gamesJson.length()) {
            val item = gamesJson.optJSONObject(index) ?: continue
            val url = item.optString("url").takeIf(String::isNotBlank) ?: continue
            val title = item.optString("title").takeIf(String::isNotBlank) ?: continue
            val storeId = item.optString("store", detectStore(url).id)
            val productId = item.optNullableString("productId") ?: item.optNullableString("titleId")
            val id = item.optString("id").takeIf(String::isNotBlank)
                ?: productId?.let { "$storeId-${it.lowercase()}" }
                ?: "$storeId-url-${ParserUtils.idFrom(url)}"
            val price = decimalField(item, "price") ?: continue
            val regular = decimalField(item, "regularPrice") ?: price
            val min = decimalField(item, "minPrice") ?: price
            val target = decimalField(item, "targetPrice")
            val addedAt = item.optLong("addedAt", now)
            val lastChecked = item.optLong("lastChecked", addedAt)
            val alerts = item.optJSONObject("alerts")
            val subscriptions = item.optJSONArray("subscriptions").toIdSet()

            games += GameEntity(
                id = id,
                storeId = storeId,
                productId = productId,
                productType = item.optNullableString("productType"),
                title = title,
                url = url,
                imageUrl = item.optNullableString("image") ?: item.optNullableString("imageUrl"),
                priceCents = price,
                regularPriceCents = regular,
                currency = item.optString("currency", "MXN"),
                region = item.optString("region", "MX"),
                discountPercent = item.optInt("discountPercent", ParserUtils.discountPercent(regular, price)),
                minPriceCents = min,
                targetPriceCents = target,
                addedAt = addedAt,
                lastChecked = lastChecked,
                lastStatus = item.optString("lastStatus", "ok"),
                lastError = item.optNullableString("lastError"),
                source = item.optString("source", "import"),
                comparisonKey = item.optString("comparisonKey").takeIf(String::isNotBlank)
                    ?: TitleNormalizer.comparisonKey(title),
                editionLabel = item.optString("edition", ProductEdition.BASE.id),
                libraryStatus = item.optString("libraryStatus", LibraryStatus.WISHLIST.id),
                ownedStoreId = item.optNullableString("ownedStore"),
                paidPriceCents = decimalField(item, "paidPrice"),
                purchaseDate = item.optNullableLong("purchaseDate"),
                gameFormat = item.optString("format", GameFormat.DIGITAL.id),
                personalRating = item.optNullableInt("rating"),
                subscriptionTags = subscriptions.joinToString(","),
                notes = item.optString("notes", ""),
                offerEndsAt = item.optNullableLong("offerEndsAt"),
                alertAnyDrop = alerts?.optBoolean("anyDrop", true) ?: true,
                alertTarget = alerts?.optBoolean("target", true) ?: true,
                alertDiscountPercent = alerts?.optNullableInt("discountPercent"),
                alertDropAmountCents = alerts?.let { decimalField(it, "dropAmount") },
                alertNewLow = alerts?.optBoolean("newLow", true) ?: true,
                alertOfferEndingSoon = alerts?.optBoolean("offerEnding", false) ?: false,
            )

            val history = item.optJSONArray("history")
            if (history != null) {
                for (historyIndex in 0 until history.length()) {
                    val point = history.optJSONObject(historyIndex) ?: continue
                    val pointPrice = decimalField(point, "price") ?: continue
                    val pointRegular = decimalField(point, "regularPrice") ?: pointPrice
                    points += PricePointEntity(
                        gameId = id,
                        priceCents = pointPrice,
                        regularPriceCents = pointRegular,
                        discountPercent = point.optInt(
                            "discountPercent",
                            ParserUtils.discountPercent(pointRegular, pointPrice),
                        ),
                        checkedAt = point.optLong("checkedAt", lastChecked),
                        source = point.optString("source", "import"),
                    )
                }
            }
        }

        val uniqueGames = games.associateBy { it.id }.values.toList()
        val validIds = uniqueGames.mapTo(mutableSetOf()) { it.id }
        val eventsJson = root.optJSONArray("events")
        if (eventsJson != null) {
            for (index in 0 until eventsJson.length()) {
                val item = eventsJson.optJSONObject(index) ?: continue
                val gameId = item.optString("gameId")
                if (gameId !in validIds) continue
                events += GameEventEntity(
                    gameId = gameId,
                    type = item.optString("type", "price_drop"),
                    title = item.optString("title", uniqueGames.firstOrNull { it.id == gameId }?.title.orEmpty()),
                    oldPriceCents = decimalField(item, "oldPrice"),
                    newPriceCents = decimalField(item, "newPrice"),
                    discountPercent = item.optInt("discountPercent", 0),
                    currency = item.optString("currency", "MXN"),
                    createdAt = item.optLong("createdAt", now),
                    seen = item.optBoolean("seen", true),
                    detail = item.optString("detail", "Evento importado."),
                )
            }
        }

        val activeSubscriptions = settings?.optJSONArray("activeSubscriptions")
            ?.toIdSet()
            ?.mapNotNull { id -> SubscriptionService.entries.firstOrNull { it.id == id } }
            ?.toSet()

        return ImportedData(
            games = uniqueGames,
            points = points.filter { it.gameId in validIds },
            events = events,
            intervalHours = settings?.optLong("intervalHours")?.takeIf { it > 0 },
            notificationsEnabled = settings?.let { if (it.has("notifications")) it.optBoolean("notifications") else null },
            themeMode = settings?.optString("theme")?.takeIf(String::isNotBlank)?.let(CheckpointThemeMode::fromId),
            monthlyBudgetCents = settings?.let { decimalField(it, "monthlyBudget") },
            activeSubscriptions = activeSubscriptions,
        )
    }

    private fun decimalField(json: JSONObject, key: String): Long? {
        if (!json.has(key) || json.isNull(key)) return null
        return ParserUtils.decimalToCents(json.opt(key))
    }

    private fun detectStore(url: String): Store = when {
        url.contains("steampowered.com", ignoreCase = true) -> Store.STEAM
        url.contains("xbox.com", ignoreCase = true) -> Store.XBOX
        else -> Store.NINTENDO
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key).takeIf(String::isNotBlank)

    private fun JSONObject.optNullableLong(key: String): Long? =
        if (!has(key) || isNull(key)) null else optLong(key)

    private fun JSONObject.optNullableInt(key: String): Int? =
        if (!has(key) || isNull(key)) null else optInt(key)

    private fun JSONObject.putNullable(key: String, value: Any?): JSONObject =
        put(key, value ?: JSONObject.NULL)

    private fun JSONArray?.toIdSet(): Set<String> {
        if (this == null) return emptySet()
        return buildSet {
            for (index in 0 until length()) optString(index).takeIf(String::isNotBlank)?.let(::add)
        }
    }
}
