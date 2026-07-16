package com.custom.dth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "channel_meta",
    indices = [Index(value = ["assignedNumber"], unique = true)]
)
data class ChannelMeta(
    @PrimaryKey
    val stableKey: String, // ONID-TSID-SID
    
    val assignedNumber: String,
    
    val customName: String? = null,
    
    val isFavorite: Boolean = false,
    
    val localLogoPath: String? = null
)
