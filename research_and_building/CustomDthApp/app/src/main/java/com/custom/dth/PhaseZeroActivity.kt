package com.custom.dth

import android.content.pm.PackageManager
import android.media.tv.TvView
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.custom.dth.core.AppEvent
import com.custom.dth.core.AppRuntime
import com.custom.dth.playback.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhaseZeroActivity : AppCompatActivity() {
    private lateinit var logTextView: TextView
    private lateinit var tvView: TvView
    
    private lateinit var appRuntime: AppRuntime
    private lateinit var playbackManager: PlaybackManager
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phase_zero)
        logTextView = findViewById(R.id.logTextView)
        tvView = findViewById(R.id.tvView)

        appendLog("=== Phase 3+ Modular Initialization ===")
        
        val permissionState = ContextCompat.checkSelfPermission(this, "android.permission.READ_TV_LISTINGS")
        
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            appendLog("Requesting READ_TV_LISTINGS permission...")
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_TV_LISTINGS"), 101)
        } else {
            startApp()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            appendLog("Permission GRANTED. Proceeding...")
            startApp()
        } else {
            appendLog("Permission DENIED. Cannot proceed.")
        }
    }

    private fun startApp() {
        appendLog("Initializing AppRuntime (Hub)...")
        appRuntime = AppRuntime(this)
        
        appendLog("Initializing PlaybackManager (Spoke)...")
        playbackManager = PlaybackManager(tvView, appRuntime.eventBus, scope) { msg ->
            appendLog(msg)
        }

        // Forward tuner changes into the Hub
        appRuntime.channelTuner.addListener(object : com.custom.dth.aosp_port.ChannelTuner.Listener {
            override fun onLoadFinished() {}
            override fun onBrowsableChannelListChanged() {}
            
            override fun onCurrentChannelUnavailable(channel: com.custom.dth.aosp_port.Channel?) {}
            
            override fun onChannelChanged(previousChannel: com.custom.dth.aosp_port.Channel?, currentChannel: com.custom.dth.aosp_port.Channel?) {
                if (currentChannel != null && currentChannel.getId() != com.custom.dth.aosp_port.Channel.INVALID_ID) {
                    val uri = android.media.tv.TvContract.buildChannelUri(currentChannel.getId())
                    appendLog("Tuner -> Hub: TuneRequested for ${currentChannel.getId()}")
                    // Emit a TuneRequest instead of tuning directly
                    appRuntime.eventBus.publish(AppEvent.TuneRequested(uri, currentChannel.getId()))
                    // For now, auto-approve since we don't have PinManager active yet
                    appRuntime.eventBus.publish(AppEvent.TuneApproved(uri, currentChannel.getId()))
                }
            }
        })

        // Log events passing through the hub
        scope.launch {
            appRuntime.eventBus.events.collect { event ->
                appendLog("Hub Event: ${event.javaClass.simpleName}")
            }
        }
        
        appendLog("Starting Runtime...")
        appRuntime.start()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!::appRuntime.isInitialized || !appRuntime.channelTuner.areAllChannelsLoaded()) {
            return super.onKeyDown(keyCode, event)
        }
        
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                appendLog("DPAD_UP -> Hub ZapUp")
                appRuntime.eventBus.publish(AppEvent.ZapUp)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                appendLog("DPAD_DOWN -> Hub ZapDown")
                appRuntime.eventBus.publish(AppEvent.ZapDown)
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                val tracks = playbackManager.getAudioTracks()
                appendLog("Audio tracks available: ${tracks.size}")
                tracks.forEach {
                    appendLog(" - Track: ${it.id} (${it.language})")
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::playbackManager.isInitialized) {
            playbackManager.release()
        }
    }

    private fun appendLog(msg: String) {
        runOnUiThread {
            Log.d("ModularApp", msg)
            logTextView.append("$msg\n")
        }
    }
}
