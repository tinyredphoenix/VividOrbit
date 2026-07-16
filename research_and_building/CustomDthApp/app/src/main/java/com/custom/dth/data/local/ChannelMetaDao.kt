package com.custom.dth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelMetaDao {
    @Query("SELECT * FROM channel_meta")
    fun getAllChannelMeta(): Flow<List<ChannelMeta>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(channelMeta: ChannelMeta)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(channelMetas: List<ChannelMeta>)

    @Update
    suspend fun update(channelMeta: ChannelMeta)

    @Query("SELECT * FROM channel_meta WHERE stableKey = :key LIMIT 1")
    suspend fun getChannelMeta(key: String): ChannelMeta?

    @Query("SELECT * FROM channel_meta WHERE assignedNumber = :number LIMIT 1")
    suspend fun getChannelMetaByNumber(number: String): ChannelMeta?
}
