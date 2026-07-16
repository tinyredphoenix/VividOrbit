package com.custom.dth.ui.core

import androidx.compose.runtime.Immutable

@Immutable
data class ChannelModel(
    val id: String,
    val systemId: Long, // Used for TvContract EPG queries
    val displayNumber: String,
    val name: String,
    val logoPath: String? = null
)

@Immutable
data class ProgramModel(
    val id: String,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val description: String = ""
)

@Immutable
data class GuideUiState(
    val channels: List<ChannelModel> = emptyList(),
    // Keyed by channel id
    val programs: Map<String, List<ProgramModel>> = emptyMap(),
    val isLoading: Boolean = false
)

@Immutable
data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val currentChannel: ChannelModel? = null,
    val currentProgram: ProgramModel? = null
)

@Immutable
data class SettingsUiState(
    val isMenuOpen: Boolean = false
)

@Immutable
data class NavigationUiState(
    val activeCategory: String? = null
)
