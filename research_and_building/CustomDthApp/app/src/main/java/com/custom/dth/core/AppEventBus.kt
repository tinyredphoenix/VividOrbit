package com.custom.dth.core

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * The centralized Event Hub for all inter-module communication.
 * Modules never hold references to each other, they only emit and collect events here.
 */
sealed class AppEvent {
    // Playback intents
    data class TuneRequested(val channelUri: Uri, val channelId: Long) : AppEvent()
    data class TuneApproved(val channelUri: Uri, val channelId: Long) : AppEvent()
    object StopPlaybackRequested : AppEvent()

    // Playback states
    data class PlaybackStarted(val channelId: Long) : AppEvent()
    data class PlaybackError(val reason: String) : AppEvent()
    object PlaybackStopped : AppEvent()

    // Key events from remote
    object ZapUp : AppEvent()
    object ZapDown : AppEvent()
    object OpenMenu : AppEvent()
}

class AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 10)
    val events = _events.asSharedFlow()

    fun publish(event: AppEvent) {
        _events.tryEmit(event)
    }
}
