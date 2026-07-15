package com.entrecheckpoints.checkpoint.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "games",
    indices = [Index("comparisonKey"), Index("libraryStatus"), Index("storeId")],
)
data class GameEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val productId: String?,
    val productType: String?,
    val title: String,
    val url: String,
    val imageUrl: String?,
    val priceCents: Long,
    val regularPriceCents: Long,
    val currency: String,
    val region: String,
    val discountPercent: Int,
    val minPriceCents: Long,
    val targetPriceCents: Long?,
    val addedAt: Long,
    val lastChecked: Long,
    val lastStatus: String,
    val lastError: String?,
    val source: String,
    val comparisonKey: String = "",
    val editionLabel: String = "base",
    val libraryStatus: String = "wishlist",
    val ownedStoreId: String? = null,
    val paidPriceCents: Long? = null,
    val purchaseDate: Long? = null,
    val gameFormat: String = "digital",
    val personalRating: Int? = null,
    val subscriptionTags: String = "",
    val notes: String = "",
    val offerEndsAt: Long? = null,
    val alertAnyDrop: Boolean = true,
    val alertTarget: Boolean = true,
    val alertDiscountPercent: Int? = null,
    val alertDropAmountCents: Long? = null,
    val alertNewLow: Boolean = true,
    val alertOfferEndingSoon: Boolean = false,
    val lastNotifiedPriceCents: Long? = null,
    val lastNotifiedAt: Long? = null,
)

@Entity(
    tableName = "price_history",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("gameId"), Index(value = ["gameId", "checkedAt"])],
)
data class PricePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val priceCents: Long,
    val regularPriceCents: Long,
    val discountPercent: Int,
    val checkedAt: Long,
    val source: String,
)

@Entity(
    tableName = "game_events",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("gameId"), Index("createdAt"), Index("seen")],
)
data class GameEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val type: String,
    val title: String,
    val oldPriceCents: Long?,
    val newPriceCents: Long?,
    val discountPercent: Int,
    val currency: String,
    val createdAt: Long,
    val seen: Boolean = false,
    val detail: String,
)

@Entity(tableName = "sync_runs", indices = [Index("finishedAt")])
data class SyncRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long,
    val updated: Int,
    val errors: Int,
    val status: String,
    val errorSummary: String?,
)
