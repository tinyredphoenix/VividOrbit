package com.custom.dth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ChannelGroup): Long

    @Query("DELETE FROM channel_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)

    @Query("SELECT * FROM channel_groups")
    fun getAllGroups(): Flow<List<ChannelGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addChannelToGroup(member: ChannelGroupMember)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND channelId = :channelId")
    suspend fun removeChannelFromGroup(groupId: Long, channelId: Long)

    @Query("SELECT channelId FROM group_members WHERE groupId = :groupId ORDER BY groupSpecificNumber ASC")
    fun getChannelIdsInGroup(groupId: Long): Flow<List<Long>>
}
