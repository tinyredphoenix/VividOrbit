package com.custom.dth.ui.features.guide

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.custom.dth.ui.components.CenterPivotList
import com.custom.dth.ui.core.ChannelModel
import com.custom.dth.ui.theme.AppSpacing

/**
 * Renders the vertically scrolling, horizontally fixed channel list.
 * Synchronizes vertical scrolling with the Program Grid.
 */
@Composable
fun GuideChannelColumn(
    channels: List<ChannelModel>,
    modifier: Modifier = Modifier
) {
    CenterPivotList(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = AppSpacing.dp16)
    ) {
        // TODO: Render channels using FocusableRow/Card components
    }
}
