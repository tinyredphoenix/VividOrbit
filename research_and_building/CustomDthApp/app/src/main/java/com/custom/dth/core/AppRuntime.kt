package com.custom.dth.core

import android.content.Context
import com.custom.dth.aosp_port.ChannelDataManagerWrapper
import com.custom.dth.aosp_port.ChannelTuner
import com.custom.dth.aosp_port.TvInputManagerHelper
import com.custom.dth.data.local.AppDatabase
import com.custom.dth.data.local.ChannelRepository
import com.custom.dth.data.local.TvProviderChannelSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service Locator and Orchestrator.
 * Wires together the event bus, database, and all independent managers.
 */
class AppRuntime(private val context: Context) {
    
    val eventBus = AppEventBus()
    private val runtimeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Data Layer
    private val database: AppDatabase = AppDatabase.getDatabase(context)
    private val channelMetaDao = database.channelMetaDao()
    private val systemSource = TvProviderChannelSource(context)
    val channelRepository = ChannelRepository(systemSource, channelMetaDao)

    // Tuner Engine
    private val channelDataManager = ChannelDataManagerWrapper(channelRepository)
    private val inputManagerHelper = TvInputManagerHelper(context)
    val channelTuner = ChannelTuner(channelDataManager, inputManagerHelper)

    // Independent Feature Managers (To be initialized in future phases)
    // private val watchHistoryManager = WatchHistoryManager(database.historyDao(), eventBus, runtimeScope)
    // private val sleepTimerManager = SleepTimerManager(eventBus, runtimeScope)
    // private val pinManager = PinManager(AppPreferences(context), eventBus, runtimeScope)

    init {
        // Wire Tuner logic to EventBus
        runtimeScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.ZapUp -> {
                        if (channelTuner.areAllChannelsLoaded()) {
                            channelTuner.moveToAdjacentBrowsableChannel(true)
                        }
                    }
                    is AppEvent.ZapDown -> {
                        if (channelTuner.areAllChannelsLoaded()) {
                            channelTuner.moveToAdjacentBrowsableChannel(false)
                        }
                    }
                    is AppEvent.TuneApproved -> {
                        // PlaybackManager will pick this up directly. 
                        // The Hub validates the tune request (e.g. Pin Manager), then approves it.
                    }
                    else -> {}
                }
            }
        }
    }

    fun start() {
        runtimeScope.launch {
            channelRepository.observeChannels().collect { channels ->
                if (channels.isNotEmpty() && !channelTuner.areAllChannelsLoaded()) {
                    channelTuner.start()
                    // Auto-zap to first channel to begin playback test
                    channelTuner.moveToAdjacentBrowsableChannel(true)
                }
            }
        }
    }
}
