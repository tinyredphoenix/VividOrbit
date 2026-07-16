package com.custom.dth.features.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntry(
    @PrimaryKey val channelId: Long,
    val watchedAtEpochMillis: Long,
    val durationMillis: Long = 0
)
