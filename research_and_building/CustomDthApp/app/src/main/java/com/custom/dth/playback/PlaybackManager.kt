package com.custom.dth.playback

import android.media.tv.TvContentRating
import android.media.tv.TvInputManager
import android.media.tv.TvTrackInfo
import android.media.tv.TvView
import android.net.Uri
import android.util.Log
import com.custom.dth.core.AppEvent
import com.custom.dth.core.AppEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PlaybackManager(
    private val tvView: TvView,
    private val eventBus: AppEventBus,
    private val scope: CoroutineScope,
    private val logCallback: (String) -> Unit
) {
    private val TAG = "PlaybackManager"
    private val dtvkitInputId = "com.droidlogic.dtvkit.inputsource/.DtvkitTvInput/HW19"
    private var currentChannelId: Long = -1

    init {
        scope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.TuneApproved -> {
                        currentChannelId = event.channelId
                        tune(event.channelUri)
                    }
                    is AppEvent.StopPlaybackRequested -> {
                        release()
                        eventBus.publish(AppEvent.PlaybackStopped)
                    }
                    else -> {}
                }
            }
        }

        tvView.setCallback(object : TvView.TvInputCallback() {
            override fun onConnectionFailed(inputId: String?) {
                logCallback("TvView: Connection Failed")
                eventBus.publish(AppEvent.PlaybackError("Connection Failed"))
            }

            override fun onDisconnected(inputId: String?) {
                logCallback("TvView: Disconnected")
                eventBus.publish(AppEvent.PlaybackError("Disconnected"))
            }

            override fun onVideoAvailable(inputId: String?) {
                logCallback("TvView: Video Available")
                if (currentChannelId != -1L) {
                    eventBus.publish(AppEvent.PlaybackStarted(currentChannelId))
                }
            }

            override fun onVideoUnavailable(inputId: String?, reason: Int) {
                val reasonStr = when (reason) {
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING -> "TUNING"
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN -> "UNKNOWN"
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL -> "WEAK_SIGNAL"
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING -> "BUFFERING"
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY -> "AUDIO_ONLY"
                    else -> reason.toString()
                }
                logCallback("TvView: Video Unavailable ($reasonStr)")
            }

            override fun onContentAllowed(inputId: String?) {
                logCallback("TvView: Content Allowed")
            }

            override fun onContentBlocked(inputId: String?, rating: TvContentRating?) {
                logCallback("TvView: Content Blocked ($rating)")
            }

            override fun onTrackSelected(inputId: String?, type: Int, trackId: String?) {
                logCallback("TvView: Track Selected (type $type, id $trackId)")
            }
        })
    }

    private fun tune(channelUri: Uri) {
        logCallback("PlaybackManager tuning to: $channelUri")
        tvView.tune(dtvkitInputId, channelUri)
    }

    fun getAudioTracks(): List<TvTrackInfo> {
        return tvView.getTracks(TvTrackInfo.TYPE_AUDIO) ?: emptyList()
    }

    fun selectAudioTrack(trackId: String) {
        logCallback("Selecting audio track: $trackId")
        tvView.selectTrack(TvTrackInfo.TYPE_AUDIO, trackId)
    }

    fun release() {
        tvView.reset()
    }
}
