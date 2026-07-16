package com.custom.dth.ui.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.custom.dth.ui.core.ChannelModel
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppRadius
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

/**
 * Screen 2 - Flow 2: Numeric Channel Entry Overlay.
 * Allows quick tuning to a channel by entering its number. Minimal and elegant.
 * 
 * RESOLUTION LOGIC:
 * The overlay always displays the entered number immediately.
 * Channel metadata appears only after a valid channel match is resolved.
 * Until then, only the entered digits are displayed.
 */
@Composable
fun NumericChannelEntry(
    enteredDigits: String,
    resolvedChannel: ChannelModel?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.dp10))
            .background(AppColors.ElevatedSurface)
            .padding(AppSpacing.dp24),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BasicText(
                text = enteredDigits,
                style = AppTypography.Display
            )
            
            if (resolvedChannel != null) {
                BasicText(
                    text = resolvedChannel.name,
                    style = AppTypography.Body.copy(color = AppColors.SecondaryText),
                    modifier = Modifier.padding(top = AppSpacing.dp8)
                )
            }
        }
    }
}
