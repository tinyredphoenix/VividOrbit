package com.custom.dth.data.local

import android.content.Context
import android.database.ContentObserver
import android.media.tv.TvContract
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Connects to the system TvProvider and emits a flow of system channels for the DTVKit input.
 */
class TvProviderChannelSource(private val context: Context) {
    private val contentResolver = context.contentResolver
    
    // The specific HW19 input ID we confirmed in Phase 0
    private val dtvkitInputId = "com.droidlogic.dtvkit.inputsource/.DtvkitTvInput/HW19"
    
    fun observeSystemChannels(): Flow<List<SystemChannel>> = callbackFlow {
        val uri = TvContract.buildChannelsUriForInput(dtvkitInputId)
        
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(fetchChannels())
            }
        }
        
        contentResolver.registerContentObserver(uri, true, observer)
        
        // Initial fetch
        trySend(fetchChannels())
        
        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }.conflate()

    private fun fetchChannels(): List<SystemChannel> {
        val uri = TvContract.buildChannelsUriForInput(dtvkitInputId)
        val channels = mutableListOf<SystemChannel>()
        
        val cursor = contentResolver.query(
            uri,
            arrayOf(
                TvContract.Channels._ID,
                TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID,
                TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID,
                TvContract.Channels.COLUMN_SERVICE_ID,
                TvContract.Channels.COLUMN_DISPLAY_NAME,
                TvContract.Channels.COLUMN_DISPLAY_NUMBER
            ),
            null,
            null,
            null
        )
        
        cursor?.use { c ->
            val idIdx = c.getColumnIndex(TvContract.Channels._ID)
            val onidIdx = c.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID)
            val tsidIdx = c.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID)
            val sidIdx = c.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID)
            val nameIdx = c.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)
            val numIdx = c.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)
            
            while (c.moveToNext()) {
                val dbId = if (idIdx != -1) c.getLong(idIdx) else -1L
                val onid = if (onidIdx != -1) c.getInt(onidIdx) else 0
                val tsid = if (tsidIdx != -1) c.getInt(tsidIdx) else 0
                val sid = if (sidIdx != -1) c.getInt(sidIdx) else 0
                val name = if (nameIdx != -1) c.getString(nameIdx) ?: "" else ""
                val num = if (numIdx != -1) c.getString(numIdx) ?: "" else ""
                
                val stableKey = "$onid-$tsid-$sid"
                channels.add(SystemChannel(dbId, stableKey, name, num))
            }
        }
        
        return channels
    }
}

data class SystemChannel(
    val id: Long,
    val stableKey: String,
    val systemName: String,
    val systemNumber: String
)
