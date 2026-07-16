package com.custom.dth.features.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WatchHistoryEntry)

    @Query("SELECT * FROM watch_history ORDER BY watchedAtEpochMillis DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<WatchHistoryEntry>>

    @Query("DELETE FROM watch_history WHERE channelId NOT IN (SELECT channelId FROM watch_history ORDER BY watchedAtEpochMillis DESC LIMIT 50)")
    suspend fun trimHistory()
}
