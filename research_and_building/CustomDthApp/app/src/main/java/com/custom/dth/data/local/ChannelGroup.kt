package com.custom.dth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "channel_groups")
data class ChannelGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "channelId"],
    foreignKeys = [
        ForeignKey(
            entity = ChannelGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["channelId"])]
)
data class ChannelGroupMember(
    val groupId: Long,
    val channelId: Long,
    val groupSpecificNumber: String? = null
)
