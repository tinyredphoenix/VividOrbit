package com.custom.dth.features.launcher

import android.content.Context
import android.media.tv.TvContract
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.custom.dth.data.local.MergedChannel

/**
 * Handles syncing selected DTH channels to the Android TV Home Screen via androidx.tvprovider.
 */
class HomeScreenChannelSync(private val context: Context) {

    /**
     * Publishes a list of channels to the Android TV Home Screen as Preview Channels.
     */
    suspend fun syncChannelsToHomeScreen(channels: List<MergedChannel>) = withContext(Dispatchers.IO) {
        channels.take(10).forEach { mergedChannel ->
            val builder = Channel.Builder()
            builder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName(mergedChannel.displayName)
                .setAppLinkIntentUri(TvContract.buildChannelUri(mergedChannel.id))
            
            val channelUri = context.contentResolver.insert(
                TvContractCompat.Channels.CONTENT_URI,
                builder.build().toContentValues()
            )
            
            // In a real app, you would also add PreviewPrograms to this channelUri
            // to show "Now Playing" or thumbnail previews on the home screen.
        }
    }
}
