package com.custom.dth.ui.features.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.custom.dth.core.AppEvent
import com.custom.dth.core.AppEventBus
import com.custom.dth.data.epg.TvProviderEpgSource
import com.custom.dth.data.local.ChannelRepository
import com.custom.dth.ui.core.ChannelModel
import com.custom.dth.ui.core.PlaybackUiState
import com.custom.dth.ui.core.ProgramModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaybackViewModel(
    private val eventBus: AppEventBus,
    private val epgSource: TvProviderEpgSource,
    private val channelRepository: ChannelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Driven entirely by PlaybackManager events (no IPTV abstractions)
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.PlaybackStarted -> handlePlaybackStarted(event.channelId)
                    is AppEvent.PlaybackStopped -> {
                        _uiState.update { it.copy(isPlaying = false, currentChannel = null, currentProgram = null) }
                    }
                    else -> {}
                }
            }
        }
    }

    private suspend fun handlePlaybackStarted(channelId: Long) {
        // Resolve channel from repository
        val channels = channelRepository.observeChannels().first()
        val mergedChannel = channels.find { it.id == channelId } ?: return
        
        val channelModel = ChannelModel(
            id = mergedChannel.stableKey,
            systemId = mergedChannel.id,
            displayNumber = mergedChannel.displayNumber,
            name = mergedChannel.displayName,
            logoPath = mergedChannel.logoPath
        )

        // Query Now and Next for the OSD
        val (nowProg, _) = epgSource.getNowAndNext(channelId)
        val programModel = nowProg?.let {
            ProgramModel(
                id = it.id.toString(),
                title = it.title,
                startTimeMillis = it.startTimeUtcMillis,
                endTimeMillis = it.endTimeUtcMillis,
                description = it.description ?: ""
            )
        }

        // In-place UI update (StateFlow ensures OSD recomposes without destruction)
        _uiState.update { 
            it.copy(
                isPlaying = true, 
                currentChannel = channelModel, 
                currentProgram = programModel 
            ) 
        }
    }
}
