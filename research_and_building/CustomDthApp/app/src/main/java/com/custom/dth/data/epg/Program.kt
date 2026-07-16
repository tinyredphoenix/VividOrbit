package com.custom.dth.data.epg

data class Program(
    val id: Long,
    val channelId: Long,
    val title: String,
    val description: String?,
    val startTimeUtcMillis: Long,
    val endTimeUtcMillis: Long
)
