package com.custom.dth.ui.features.guide

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.custom.dth.ui.core.GuideUiState

// =========================================================================
// REGRESSION PREVIEWS
// =========================================================================
// These previews form the visual regression suite for the Guide architecture.
// Do not modify the UX to fit the framework. If these break, the implementation
// must be fixed to match the specification.

@Preview(name = "1080p Dark Theme", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewGuide1080p() {
    val (channels, programs) = MockDataGenerator.generateMockGuideData()
    GuideCoordinator(
        uiState = GuideUiState(channels = channels.take(10), programs = programs)
    )
}

@Preview(name = "720p Dark Theme", device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewGuide720p() {
    val (channels, programs) = MockDataGenerator.generateMockGuideData()
    GuideCoordinator(
        uiState = GuideUiState(channels = channels.take(10), programs = programs)
    )
}

@Preview(name = "4K Dark Theme", device = "spec:width=3840,height=2160,dpi=320,isTv=true", showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewGuide4K() {
    val (channels, programs) = MockDataGenerator.generateMockGuideData()
    GuideCoordinator(
        uiState = GuideUiState(channels = channels.take(10), programs = programs)
    )
}

@Preview(name = "Font Scale 1.5x", device = Devices.TV_1080p, fontScale = 1.5f, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewGuideFontScale() {
    val (channels, programs) = MockDataGenerator.generateMockGuideData()
    GuideCoordinator(
        uiState = GuideUiState(channels = channels.take(10), programs = programs)
    )
}

// TODO: Add Overscan Safe Area preview (using appropriate WindowInsets in the root scaffold)
