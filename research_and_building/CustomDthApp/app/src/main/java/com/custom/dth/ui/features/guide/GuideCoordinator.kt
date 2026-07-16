package com.custom.dth.ui.features.guide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.custom.dth.ui.core.GuideUiState

/**
 * The root controller for the EPG.
 * Orchestrates the layout of the Timeline, Channel Column, and Program Grid.
 * Synchronizes scroll states across components to ensure 2D movement.
 */
@Composable
fun GuideCoordinator(
    uiState: GuideUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        GuideTimeline(
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.weight(1f)) {
            GuideChannelColumn(
                channels = uiState.channels,
                modifier = Modifier.weight(0.29f) // 29% width as per spec
            )
            GuideProgramGrid(
                programsByChannel = uiState.programs,
                modifier = Modifier.weight(0.71f) // 71% width as per spec
            )
        }
    }
}
