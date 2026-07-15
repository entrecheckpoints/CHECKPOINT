package com.entrecheckpoints.checkpoint.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GameEntity::class, PricePointEntity::class, GameEventEntity::class, SyncRunEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class CheckpointDatabase : RoomDatabase() {
    abstract fun checkpointDao(): CheckpointDao

    companion object {
        @Volatile private var instance: CheckpointDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE games ADD COLUMN comparisonKey TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE games ADD COLUMN editionLabel TEXT NOT NULL DEFAULT 'base'")
                database.execSQL("ALTER TABLE games ADD COLUMN libraryStatus TEXT NOT NULL DEFAULT 'wishlist'")
                database.execSQL("ALTER TABLE games ADD COLUMN ownedStoreId TEXT")
                database.execSQL("ALTER TABLE games ADD COLUMN paidPriceCents INTEGER")
                database.execSQL("ALTER TABLE games ADD COLUMN purchaseDate INTEGER")
                database.execSQL("ALTER TABLE games ADD COLUMN gameFormat TEXT NOT NULL DEFAULT 'digital'")
                database.execSQL("ALTER TABLE games ADD COLUMN personalRating INTEGER")
                database.execSQL("ALTER TABLE games ADD COLUMN subscriptionTags TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE games ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE games ADD COLUMN offerEndsAt INTEGER")
                database.execSQL("ALTER TABLE games ADD COLUMN alertAnyDrop INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE games ADD COLUMN alertTarget INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE games ADD COLUMN alertDiscountPercent INTEGER")
                database.execSQL("ALTER TABLE games ADD COLUMN alertDropAmountCents INTEGER")
                database.execSQL("ALTER TABLE games ADD COLUMN alertNewLow INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE games ADD COLUMN alertOfferEndingSoon INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE games ADD COLUMN lastNotifiedPriceCents INTEGER")
                database.execSQL("ALTER TABLE games ADD COLUMN lastNotifiedAt INTEGER")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_games_comparisonKey ON games(comparisonKey)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_games_libraryStatus ON games(libraryStatus)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_games_storeId ON games(storeId)")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS game_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        gameId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        oldPriceCents INTEGER,
                        newPriceCents INTEGER,
                        discountPercent INTEGER NOT NULL,
                        currency TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        seen INTEGER NOT NULL,
                        detail TEXT NOT NULL,
                        FOREIGN KEY(gameId) REFERENCES games(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_game_events_gameId ON game_events(gameId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_game_events_createdAt ON game_events(createdAt)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_game_events_seen ON game_events(seen)")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_runs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        finishedAt INTEGER NOT NULL,
                        updated INTEGER NOT NULL,
                        errors INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        errorSummary TEXT
                    )
                    """.trimIndent(),
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_runs_finishedAt ON sync_runs(finishedAt)")
            }
        }

        fun get(context: Context): CheckpointDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CheckpointDatabase::class.java,
                "checkpoint.db",
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }
}
