package com.custom.dth

import android.content.pm.PackageManager
import android.media.tv.TvContract
import android.media.tv.TvView
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.custom.dth.aosp_port.Channel
import com.custom.dth.aosp_port.ChannelDataManagerWrapper
import com.custom.dth.aosp_port.ChannelTuner
import com.custom.dth.aosp_port.TvInputManagerHelper
import com.custom.dth.data.local.AppDatabase
import com.custom.dth.data.local.ChannelRepository
import com.custom.dth.data.local.TvProviderChannelSource
import com.custom.dth.playback.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhaseZeroActivity : AppCompatActivity() {
    private lateinit var logTextView: TextView
    private lateinit var tvView: TvView
    private lateinit var playbackManager: PlaybackManager
    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var channelTuner: ChannelTuner
    private var testRun = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phase_zero)
        logTextView = findViewById(R.id.logTextView)
        tvView = findViewById(R.id.tvView)

        playbackManager = PlaybackManager(tvView) { msg ->
            appendLog(msg)
        }

        appendLog("=== Phase 2 Playback Verification ===")
        
        val permissionState = ContextCompat.checkSelfPermission(this, "android.permission.READ_TV_LISTINGS")
        
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            appendLog("Requesting READ_TV_LISTINGS permission...")
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_TV_LISTINGS"), 101)
        } else {
            startPhaseTwoTest()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            appendLog("Permission GRANTED. Proceeding...")
            startPhaseTwoTest()
        } else {
            appendLog("Permission DENIED. Cannot proceed.")
        }
    }

    private fun startPhaseTwoTest() {
        appendLog("Initializing Room Database...")
        val database = AppDatabase.getDatabase(this)
        val channelMetaDao = database.channelMetaDao()

        appendLog("Initializing TvProviderChannelSource & Repository...")
        val systemSource = TvProviderChannelSource(this)
        val repository = ChannelRepository(systemSource, channelMetaDao)

        appendLog("Initializing ChannelDataManagerWrapper...")
        val channelDataManager = ChannelDataManagerWrapper(repository)
        val inputManagerHelper = TvInputManagerHelper(this)
        
        channelTuner = ChannelTuner(channelDataManager, inputManagerHelper)
        
        channelTuner.addListener(object : ChannelTuner.Listener {
            override fun onLoadFinished() {}
            override fun onBrowsableChannelListChanged() {}
            
            override fun onCurrentChannelUnavailable(channel: Channel?) {
                appendLog("ChannelTuner: current channel unavailable.")
            }
            
            override fun onChannelChanged(previousChannel: Channel?, currentChannel: Channel?) {
                if (currentChannel != null && currentChannel.getId() != Channel.INVALID_ID) {
                    val uri = TvContract.buildChannelUri(currentChannel.getId())
                    appendLog("ChannelTuner moved to: ${currentChannel.getId()} - Tuning TvView...")
                    playbackManager.tune(uri)
                }
            }
        })
        
        scope.launch {
            repository.observeChannels().collect { channels ->
                if (!testRun && channels.isNotEmpty()) {
                    testRun = true
                    appendLog("Repository emitted ${channels.size} channels.")
                    
                    withContext(Dispatchers.IO) { Thread.sleep(500) }
                    
                    appendLog("Starting ChannelTuner...")
                    channelTuner.start()
                    
                    // Trigger the first tune!
                    appendLog("Auto-tuning to the first available channel...")
                    val success = channelTuner.moveToAdjacentBrowsableChannel(true)
                    if (!success) {
                        appendLog("WARNING: No browsable channel found to auto-tune.")
                    }
                }
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!::channelTuner.isInitialized || !channelTuner.areAllChannelsLoaded()) {
            return super.onKeyDown(keyCode, event)
        }
        
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                appendLog("DPAD_UP pressed -> moveToAdjacent(true)")
                channelTuner.moveToAdjacentBrowsableChannel(true)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                appendLog("DPAD_DOWN pressed -> moveToAdjacent(false)")
                channelTuner.moveToAdjacentBrowsableChannel(false)
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
        playbackManager.release()
    }

    private fun appendLog(msg: String) {
        runOnUiThread {
            Log.d("PhaseTwo", msg)
            logTextView.append("$msg\n")
        }
    }
}
