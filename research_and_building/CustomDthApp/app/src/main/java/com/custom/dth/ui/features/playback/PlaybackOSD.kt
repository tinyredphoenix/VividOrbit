package com.custom.dth.ui.features.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.custom.dth.ui.core.ChannelModel
import com.custom.dth.ui.core.ProgramModel
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

/**
 * Screen 2 - Flow 1: Playback Information Overlay.
 * Primary on-screen display (OSD) shown during live TV playback. Auto-hides after a set duration.
 * 
 * IN-PLACE UPDATES:
 * If the overlay is already visible and new playback information arrives (e.g., rapid channel switching),
 * the existing overlay updates in place. It must never be destroyed and recreated.
 */
@Composable
fun PlaybackOSD(
    currentChannel: ChannelModel?,
    currentProgram: ProgramModel?,
    modifier: Modifier = Modifier
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color(0xFF0A0C0F).copy(alpha = 0.9f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundBrush)
            .padding(AppSpacing.dp32),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            BasicText(
                text = currentChannel?.let { "${it.displayNumber} ${it.name}" } ?: "",
                style = AppTypography.Title
            )
            BasicText(
                text = currentProgram?.title ?: "No Information",
                style = AppTypography.Body.copy(color = AppColors.SecondaryText),
                modifier = Modifier.padding(top = AppSpacing.dp8)
            )
        }
    }
}
