package com.custom.dth.ui.features.guide

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized metrics for the Guide EPG.
 * Ensures consistent alignment across Timeline, Channel Column, and Program Grid.
 */
object GuideLayoutMetrics {
    // Spatial mapping: 1 minute = 8dp width
    // A 30 min program will be 240.dp wide
    const val DP_PER_MINUTE = 8f
    
    // Vertical definitions
    val RowHeight = 72.dp
    val TimelineHeight = 56.dp
    
    // Horizontal definitions
    val ChannelColumnWidth = 240.dp
    
    // Spacing
    val SpacingBetweenRows = 8.dp
    val SpacingBetweenPrograms = 4.dp
    val GuideStartPadding = 32.dp
    
    /**
     * Calculates the width of a program cell based on its duration.
     */
    fun durationToWidth(durationMillis: Long): Dp {
        val minutes = durationMillis / 60000f
        return (minutes * DP_PER_MINUTE).dp
    }
}
