package com.custom.dth.data.local

import android.content.Context
import android.media.tv.TvContract
import android.net.Uri
import com.custom.dth.R
import java.io.File

class ChannelLogoResolver(private val context: Context) {
    /**
     * Resolves a logo using a 3-tier fallback:
     * 1. Check local file override.
     * 2. Check TvProvider standard URI.
     * 3. Fallback to app placeholder.
     */
    fun resolveLogo(channelId: Long, localLogoPath: String?): Uri {
        // 1. Local override (if explicitly set by user)
        if (!localLogoPath.isNullOrEmpty()) {
            val file = File(localLogoPath)
            if (file.exists()) {
                return Uri.fromFile(file)
            }
        }

        // 2. Broadcast/TvProvider default
        // The UI layer (e.g. Glide or Coil) will automatically resolve Content URIs.
        return TvContract.buildChannelLogoUri(channelId)
        
        // 3. Glide/Coil will fall back to error() placeholder if the TvProvider URI fails.
    }
}
