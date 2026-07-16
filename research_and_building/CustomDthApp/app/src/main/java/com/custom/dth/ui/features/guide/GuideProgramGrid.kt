package com.custom.dth.ui.features.guide

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    // TODO: Implement custom layout measuring cell widths based on duration.
    // Ensure horizontal scrolling tracks the timeline.
    // Ensure vertical scrolling tracks the channel column.
}
