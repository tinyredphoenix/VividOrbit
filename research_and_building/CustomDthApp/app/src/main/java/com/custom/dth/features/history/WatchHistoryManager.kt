package com.custom.dth.features.history

import com.custom.dth.core.AppEvent
import com.custom.dth.core.AppEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class WatchHistoryManager(
    private val historyDao: HistoryDao,
    private val eventBus: AppEventBus,
    private val scope: CoroutineScope
) {
    init {
        scope.launch {
            eventBus.events.collect { event ->
                if (event is AppEvent.PlaybackStarted) {
                    recordTune(event.channelId)
                }
            }
        }
    }

    private suspend fun recordTune(channelId: Long) {
        val entry = WatchHistoryEntry(
            channelId = channelId,
            watchedAtEpochMillis = System.currentTimeMillis()
        )
        historyDao.upsert(entry)
        // Trim history to maintain only the last 50 entries
        historyDao.trimHistory()
    }
}
