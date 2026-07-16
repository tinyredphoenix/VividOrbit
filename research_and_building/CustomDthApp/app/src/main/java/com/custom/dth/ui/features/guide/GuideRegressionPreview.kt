package com.custom.dth.ui.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.core.ChannelModel
import com.custom.dth.ui.core.GuideUiState
import com.custom.dth.ui.features.guide.overlays.CategoryPanel
import com.custom.dth.ui.features.guide.overlays.ContextActionsPanel
import com.custom.dth.ui.features.guide.overlays.NavigationRail
import com.custom.dth.ui.features.playback.NumericChannelEntry
import com.custom.dth.ui.features.playback.PlaybackOSD
import com.custom.dth.ui.features.settings.SettingsList

// =========================================================================
// REGRESSION PREVIEWS
// =========================================================================
// These previews form the visual regression suite for the entire UI architecture.
// Do not modify the UX to fit the framework. If these break, the implementation
// must be fixed to match the specification.

@Preview(name = "1. Guide Only", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewGuideOnly() {
    val (channels, programs) = MockDataGenerator.generateMockGuideData()
    GuideCoordinator(uiState = GuideUiState(channels = channels.take(10), programs = programs))
}

@Preview(name = "2. Guide + Category", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewGuideCategory() {
    val (channels, programs) = MockDataGenerator.generateMockGuideData()
    Box(modifier = Modifier.fillMaxSize()) {
        GuideCoordinator(uiState = GuideUiState(channels = channels.take(10), programs = programs))
        CategoryPanel(
            categories = listOf("All Channels", "HD", "Movies", "News", "Sports", "Kids"),
            onCategoryFocused = {},
            onCloseRequested = {},
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Preview(name = "3. Guide + Navigation Rail", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewGuideNavRail() {
    val (channels, programs) = MockDataGenerator.generateMockGuideData()
    Box(modifier = Modifier.fillMaxSize()) {
        GuideCoordinator(uiState = GuideUiState(channels = channels.take(10), programs = programs))
        NavigationRail(
            destinations = listOf("Live TV", "Catch Up", "VOD", "Settings"),
            onDestinationSelected = {},
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Preview(name = "4. Guide + Context Actions", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewGuideContextActions() {
    val (channels, programs) = MockDataGenerator.generateMockGuideData()
    Box(modifier = Modifier.fillMaxSize()) {
        GuideCoordinator(uiState = GuideUiState(channels = channels.take(10), programs = programs))
        ContextActionsPanel(
            actions = listOf("Add to Favorites", "Lock Channel", "More Info"),
            onActionSelected = {},
            modifier = Modifier.align(Alignment.Center).padding(start = 200.dp) // Offset slightly from center
        )
    }
}

@Preview(name = "5. Playback OSD", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewPlaybackOSD() {
    Box(modifier = Modifier.fillMaxSize()) {
        PlaybackOSD(
            currentChannel = ChannelModel("ch_1", "101", "Star Movies", null),
            currentProgram = MockDataGenerator.generateMockGuideData().second.values.first().first(),
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Preview(name = "6. Numeric Entry", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewNumericEntry() {
    Box(modifier = Modifier.fillMaxSize()) {
        NumericChannelEntry(
            enteredDigits = "101",
            resolvedChannel = ChannelModel("ch_1", "101", "Star Movies", null),
            modifier = Modifier.align(Alignment.TopEnd).padding(48.dp)
        )
    }
}

@Preview(name = "7. Settings Home", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewSettingsHome() {
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsList(
            settingsCategories = listOf("Video", "Audio", "Parental Controls", "System"),
            onCategorySelected = {},
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Preview(name = "8. Settings Category", device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF15181D)
@Composable
fun PreviewSettingsCategory() {
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsList(
            settingsCategories = listOf("Aspect Ratio", "Resolution", "HDR Mode", "HDMI CEC"),
            onCategorySelected = {},
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
