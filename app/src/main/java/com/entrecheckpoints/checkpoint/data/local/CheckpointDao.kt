package com.entrecheckpoints.checkpoint.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckpointDao {
    @Query("SELECT * FROM games ORDER BY addedAt DESC")
    fun observeGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM price_history WHERE gameId = :gameId ORDER BY checkedAt ASC")
    fun observeHistory(gameId: String): Flow<List<PricePointEntity>>

    @Query("SELECT * FROM price_history ORDER BY checkedAt ASC")
    fun observeAllHistory(): Flow<List<PricePointEntity>>

    @Query("SELECT * FROM game_events ORDER BY createdAt DESC LIMIT 200")
    fun observeEvents(): Flow<List<GameEventEntity>>

    @Query("SELECT * FROM sync_runs ORDER BY finishedAt DESC LIMIT 30")
    fun observeSyncRuns(): Flow<List<SyncRunEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGame(id: String): GameEntity?

    @Query("SELECT * FROM games WHERE storeId = :storeId AND productId = :productId LIMIT 1")
    suspend fun findByProduct(storeId: String, productId: String): GameEntity?

    @Query("SELECT * FROM games WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): GameEntity?

    @Query("SELECT * FROM games ORDER BY addedAt DESC")
    suspend fun getAllGames(): List<GameEntity>

    @Query("SELECT * FROM price_history WHERE gameId = :gameId ORDER BY checkedAt ASC")
    suspend fun getHistory(gameId: String): List<PricePointEntity>

    @Query("SELECT * FROM price_history ORDER BY checkedAt ASC")
    suspend fun getAllHistory(): List<PricePointEntity>

    @Query("SELECT * FROM game_events ORDER BY createdAt DESC")
    suspend fun getAllEvents(): List<GameEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGame(game: GameEntity)

    @Insert
    suspend fun insertHistory(point: PricePointEntity)

    @Insert
    suspend fun insertHistory(points: List<PricePointEntity>)

    @Insert
    suspend fun insertEvents(events: List<GameEventEntity>)

    @Insert
    suspend fun insertSyncRun(run: SyncRunEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGame(id: String)

    @Query("UPDATE games SET targetPriceCents = :target WHERE id = :id")
    suspend fun updateTarget(id: String, target: Long?)

    @Query("UPDATE game_events SET seen = 1")
    suspend fun markAllEventsSeen()

    @Query("SELECT COUNT(*) > 0 FROM game_events WHERE gameId = :gameId AND type = :type AND createdAt >= :since")
    suspend fun hasRecentEvent(gameId: String, type: String, since: Long): Boolean

    @Query("DELETE FROM price_history WHERE gameId = :gameId AND id NOT IN (SELECT id FROM price_history WHERE gameId = :gameId ORDER BY checkedAt DESC LIMIT :maxRows)")
    suspend fun trimHistory(gameId: String, maxRows: Int)

    @Query("DELETE FROM game_events WHERE id NOT IN (SELECT id FROM game_events ORDER BY createdAt DESC LIMIT :maxRows)")
    suspend fun trimEvents(maxRows: Int)

    @Query("DELETE FROM sync_runs WHERE id NOT IN (SELECT id FROM sync_runs ORDER BY finishedAt DESC LIMIT :maxRows)")
    suspend fun trimSyncRuns(maxRows: Int)

    @Transaction
    suspend fun replaceEverything(
        games: List<GameEntity>,
        points: List<PricePointEntity>,
        events: List<GameEventEntity>,
    ) {
        clearEvents()
        clearHistory()
        clearGames()
        games.forEach { upsertGame(it) }
        if (points.isNotEmpty()) insertHistory(points)
        if (events.isNotEmpty()) insertEvents(events)
    }

    @Query("DELETE FROM game_events")
    suspend fun clearEvents()

    @Query("DELETE FROM price_history")
    suspend fun clearHistory()

    @Query("DELETE FROM games")
    suspend fun clearGames()
}
