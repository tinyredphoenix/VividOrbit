package com.custom.dth.ui.features.playback.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.components.tvFocus
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

@Composable
fun AudioTrackSelector(
    onCloseRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialFocus = remember { FocusRequester() }
    
    // In reality, this comes from PlaybackViewModel
    val dummyTracks = listOf("English", "Hindi", "Tamil", "Telugu")
    val selectedTrack = "English"

    LaunchedEffect(Unit) {
        initialFocus.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.PrimaryBackground.copy(alpha = 0.5f))
            .onKeyEvent {
                if (it.key == Key.Back || it.key == Key.Escape) {
                    onCloseRequested()
                    true
                } else false
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .background(AppColors.ElevatedSurface)
                .padding(AppSpacing.dp24)
        ) {
            BasicText(
                text = "Audio Tracks",
                style = AppTypography.Title.copy(color = AppColors.PrimaryText),
                modifier = Modifier.padding(bottom = AppSpacing.dp24)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.dp8)
            ) {
                items(dummyTracks) { track ->
                    val isSelected = track == selectedTrack
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .tvFocus()
                            .then(if (isSelected) Modifier.focusRequester(initialFocus) else Modifier)
                            .padding(AppSpacing.dp16)
                    ) {
                        BasicText(
                            text = track + if (isSelected) " (Active)" else "",
                            style = AppTypography.Body.copy(color = if (isSelected) AppColors.AccentBlue else AppColors.PrimaryText)
                        )
                    }
                }
            }
        }
    }
}
