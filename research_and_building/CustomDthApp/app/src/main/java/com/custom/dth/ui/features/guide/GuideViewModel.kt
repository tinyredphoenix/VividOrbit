package com.custom.dth.ui.features.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.custom.dth.data.epg.TvProviderEpgSource
import com.custom.dth.data.local.ChannelRepository
import com.custom.dth.ui.core.ChannelModel
import com.custom.dth.ui.core.GuideUiState
import com.custom.dth.ui.core.ProgramModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuideViewModel(
    private val channelRepository: ChannelRepository,
    private val epgSource: TvProviderEpgSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuideUiState(isLoading = true))
    val uiState: StateFlow<GuideUiState> = _uiState.asStateFlow()

    // 2-hour window by default
    private val windowDurationMillis = 2 * 60 * 60 * 1000L
    private var currentStartTimeMillis = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            // React dynamically to channel changes without full rebuilds
            channelRepository.observeChannels().collectLatest { mergedChannels ->
                val uiChannels = mergedChannels.map {
                    ChannelModel(
                        id = it.stableKey,
                        systemId = it.id,
                        displayNumber = it.displayNumber,
                        name = it.displayName,
                        logoPath = it.logoPath
                    )
                }
                
                _uiState.update { it.copy(channels = uiChannels, isLoading = false) }
                
                // Fetch initial EPG for the current window
                fetchEpgWindow(uiChannels)
            }
        }
    }

    /**
     * Loads only the EPG window required.
     * Called when the timeline shifts significantly or on initial load.
     */
    fun shiftEpgWindow(newStartTimeMillis: Long) {
        currentStartTimeMillis = newStartTimeMillis
        viewModelScope.launch {
            fetchEpgWindow(_uiState.value.channels)
        }
    }

    private suspend fun fetchEpgWindow(channels: List<ChannelModel>) {
        if (channels.isEmpty()) return

        val endTimeMillis = currentStartTimeMillis + windowDurationMillis
        val newProgramsMap = _uiState.value.programs.toMutableMap()
        
        // For hardware DTV, we load directly from the provider.
        // We only fetch for a bounded window to keep memory and CPU light.
        channels.forEach { channel ->
            val programs = epgSource.getPrograms(
                channelId = channel.systemId, 
                startTimeMillis = currentStartTimeMillis, 
                endTimeMillis = endTimeMillis
            )
            
            newProgramsMap[channel.id] = programs.map { prog ->
                ProgramModel(
                    id = prog.id.toString(),
                    title = prog.title,
                    startTimeMillis = prog.startTimeUtcMillis,
                    endTimeMillis = prog.endTimeUtcMillis,
                    description = prog.description ?: ""
                )
            }
        }
        
        // Update state immutably; compose will smartly recompose only changed nodes
        _uiState.update { it.copy(programs = newProgramsMap) }
    }
}
