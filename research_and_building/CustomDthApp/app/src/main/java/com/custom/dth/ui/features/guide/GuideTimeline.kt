package com.custom.dth.ui.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.theme.AppColors

/**
 * Renders the horizontal time strip across the top of the EPG.
 * Height is approx 6% of the guide layout height.
 * Scrolls horizontally in sync with the Program Grid.
 */
@Composable
fun GuideTimeline(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .background(AppColors.SecondarySurface)
    ) {
        // TODO: Render current date, time, and future time markers.
    }
}
