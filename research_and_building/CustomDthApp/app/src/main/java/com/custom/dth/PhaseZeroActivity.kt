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
import androidx.compose.ui.platform.ComposeView
import com.custom.dth.data.epg.TvProviderEpgSource
import com.custom.dth.ui.core.OverlayContainer
import com.custom.dth.ui.features.guide.GuideViewModel
import com.custom.dth.ui.features.playback.PlaybackViewModel
import com.custom.dth.ui.core.TvFocusManager
import com.custom.dth.ui.core.OverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.custom.dth.ui.features.guide.GuideCoordinator

class PhaseZeroActivity : AppCompatActivity() {
    private lateinit var composeView: ComposeView
    private lateinit var tvView: TvView
    
    private lateinit var appRuntime: AppRuntime
    private lateinit var playbackManager: PlaybackManager
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private lateinit var guideViewModel: GuideViewModel
    private lateinit var playbackViewModel: PlaybackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phase_zero)
        composeView = findViewById(R.id.composeView)
        tvView = findViewById(R.id.tvView)
        
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
        appRuntime = AppRuntime(this)
        
        playbackManager = PlaybackManager(tvView, appRuntime.eventBus, scope) { msg ->
            Log.d("ModularApp", msg)
        }
        
        // Initialize ViewModels
        val epgSource = TvProviderEpgSource(this)
        guideViewModel = GuideViewModel(appRuntime.channelRepository, epgSource)
        playbackViewModel = PlaybackViewModel(appRuntime.eventBus, epgSource, appRuntime.channelRepository)
        
        // Bind Compose UI
        composeView.setContent {
            val overlayManager = androidx.compose.runtime.remember { OverlayManager() }
            val focusManager = com.custom.dth.ui.core.rememberTvFocusManager()
            
            OverlayContainer(
                overlayManager = overlayManager,
                focusManager = focusManager
            ) {
                val uiState by guideViewModel.uiState.collectAsState()
                GuideCoordinator(uiState = uiState)
            }
        }

        // Forward tuner changes into the Hub
        appRuntime.channelTuner.addListener(object : com.custom.dth.aosp_port.ChannelTuner.Listener {
            override fun onLoadFinished() {}
            override fun onBrowsableChannelListChanged() {}
            
            override fun onCurrentChannelUnavailable(channel: com.custom.dth.aosp_port.Channel?) {}
            
            override fun onChannelChanged(previousChannel: com.custom.dth.aosp_port.Channel?, currentChannel: com.custom.dth.aosp_port.Channel?) {
                if (currentChannel != null && currentChannel.getId() != com.custom.dth.aosp_port.Channel.INVALID_ID) {
                    val uri = android.media.tv.TvContract.buildChannelUri(currentChannel.getId())
                    Log.d("ModularApp", "Tuner -> Hub: TuneRequested for ${currentChannel.getId()}")
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
                Log.d("ModularApp", "Hub Event: ${event.javaClass.simpleName}")
            }
        }
        
        Log.d("ModularApp", "Starting Runtime...")
        appRuntime.start()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!::appRuntime.isInitialized || !appRuntime.channelTuner.areAllChannelsLoaded()) {
            return super.onKeyDown(keyCode, event)
        }
        
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                Log.d("ModularApp", "DPAD_UP -> Hub ZapUp")
                appRuntime.eventBus.publish(AppEvent.ZapUp)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                Log.d("ModularApp", "DPAD_DOWN -> Hub ZapDown")
                appRuntime.eventBus.publish(AppEvent.ZapDown)
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                Log.d("ModularApp", "ENTER -> Hub OpenMenu")
                appRuntime.eventBus.publish(AppEvent.OpenMenu)
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
        Log.d("ModularApp", msg)
    }
}
