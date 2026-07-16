package com.custom.dth.ui.features.guide

import com.custom.dth.ui.core.ChannelModel
import com.custom.dth.ui.core.ProgramModel

object MockDataGenerator {
    private const val MINUTE_MS = 60000L
    private const val HOUR_MS = 60 * MINUTE_MS
    
    fun generateMockGuideData(): Pair<List<ChannelModel>, Map<String, List<ProgramModel>>> {
        val channels = mutableListOf<ChannelModel>()
        val programsMap = mutableMapOf<String, List<ProgramModel>>()
        
        val baseTime = System.currentTimeMillis() - (System.currentTimeMillis() % HOUR_MS) // Start of current hour
        
        // 1. Stress Test: 1000 Channels
        for (i in 1..1000) {
            val channelId = "ch_$i"
            channels.add(
                ChannelModel(
                    id = channelId,
                    systemId = i.toLong(),
                    displayNumber = String.format("%03d", i),
                    name = "Channel $i",
                    logoPath = if (i % 5 == 0) null else "mock_logo_url" // Missing logo test
                )
            )
            
            // Generate programs for each channel
            val channelPrograms = mutableListOf<ProgramModel>()
            var currentProgramTime = baseTime - (2 * HOUR_MS) // Start 2 hours ago
            
            when {
                i == 1 -> {
                    // Normal 15/30 min intervals
                    for (j in 0..20) {
                        val duration = if (j % 2 == 0) 30 * MINUTE_MS else 15 * MINUTE_MS
                        channelPrograms.add(createProgram(channelId, j, currentProgramTime, duration))
                        currentProgramTime += duration
                    }
                }
                i == 2 -> {
                    // Very long movie (4 hours)
                    channelPrograms.add(createProgram(channelId, 1, currentProgramTime, 4 * HOUR_MS))
                    currentProgramTime += 4 * HOUR_MS
                    channelPrograms.add(createProgram(channelId, 2, currentProgramTime, 30 * MINUTE_MS))
                }
                i == 3 -> {
                    // Short clips (5 minutes)
                    for (j in 0..50) {
                        channelPrograms.add(createProgram(channelId, j, currentProgramTime, 5 * MINUTE_MS))
                        currentProgramTime += 5 * MINUTE_MS
                    }
                }
                i == 4 -> {
                    // Missing EPG Gap test
                    channelPrograms.add(createProgram(channelId, 1, currentProgramTime, 30 * MINUTE_MS))
                    currentProgramTime += 30 * MINUTE_MS
                    // 1.5 hour gap here!
                    currentProgramTime += 90 * MINUTE_MS 
                    channelPrograms.add(createProgram(channelId, 2, currentProgramTime, 60 * MINUTE_MS))
                }
                else -> {
                    // Random mix to stress test overlaps
                    for (j in 0..10) {
                        val duration = listOf(15, 30, 45, 60).random() * MINUTE_MS
                        channelPrograms.add(createProgram(channelId, j, currentProgramTime, duration))
                        currentProgramTime += duration
                    }
                }
            }
            programsMap[channelId] = channelPrograms
        }
        
        return Pair(channels, programsMap)
    }
    
    private fun createProgram(channelId: String, index: Int, startTime: Long, duration: Long): ProgramModel {
        return ProgramModel(
            id = "${channelId}_prog_$index",
            title = "Program ${index + 1} (${duration / MINUTE_MS}m)",
            startTimeMillis = startTime,
            endTimeMillis = startTime + duration,
            description = "Description for program $index on channel $channelId."
        )
    }
}
