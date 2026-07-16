package com.custom.dth.ui.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.components.CenterPivotList
import com.custom.dth.ui.core.ProgramModel

/**
 * Renders the 2D program cells (width proportional to duration).
 * Synchronized with the timeline (horizontal) and channel column (vertical).
 */
@Composable
fun GuideProgramGrid(
    programsByChannel: Map<String, List<ProgramModel>>,
    modifier: Modifier = Modifier
) {
    // To ensure perfect vertical sync with the GuideChannelColumn, we use CenterPivotList here as well.
    // The scroll states of these two CenterPivotLists will be synchronized at the Coordinator level.
    CenterPivotList(modifier = modifier) {
        items(programsByChannel.size) { index ->
            val channelId = programsByChannel.keys.elementAt(index)
            val programs = programsByChannel[channelId] ?: emptyList()
            
            // Each row handles its own layout based on program start times
            GuideProgramRow(programs = programs)
        }
    }
}

@Composable
fun GuideProgramRow(programs: List<ProgramModel>) {
    // We use a Box and absolute offsets here, or a custom Layout, to place programs precisely.
    // Width is calculated via GuideLayoutMetrics.
    Box(modifier = Modifier.height(GuideLayoutMetrics.RowHeight)) {
        programs.forEach { program ->
            // In a real implementation, calculate exact X offset based on baseTime and startTime
            // val offsetX = GuideLayoutMetrics.durationToWidth(program.startTimeMillis - baseTime)
            val width = GuideLayoutMetrics.durationToWidth(program.endTimeMillis - program.startTimeMillis)
            
            Box(
                modifier = Modifier
                    .width(width)
                    .padding(GuideLayoutMetrics.SpacingBetweenPrograms)
                    // .offset(x = offsetX)
            ) {
                // Program Cell UI (FocusableCard) goes here
            }
        }
    }
}
