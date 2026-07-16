package com.custom.dth.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun ImportExportOverlay(
    onCloseRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialFocus = remember { FocusRequester() }
    var statusMessage by remember { mutableStateOf<String?>(null) }

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
                .width(350.dp)
                .fillMaxHeight()
                .background(AppColors.ElevatedSurface)
                .padding(AppSpacing.dp24)
        ) {
            BasicText(
                text = "Backup & Restore",
                style = AppTypography.Title.copy(color = AppColors.PrimaryText),
                modifier = Modifier.padding(bottom = AppSpacing.dp24)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.dp16)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocus()
                        .focusRequester(initialFocus)
                        .padding(AppSpacing.dp16)
                ) {
                    BasicText("Backup to External Storage", style = AppTypography.Body.copy(color = AppColors.PrimaryText))
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocus()
                        .padding(AppSpacing.dp16)
                ) {
                    BasicText("Restore from Backup", style = AppTypography.Body.copy(color = AppColors.PrimaryText))
                }
            }
            
            if (statusMessage != null) {
                BasicText(
                    text = statusMessage!!,
                    style = AppTypography.Caption.copy(color = AppColors.SecondaryText),
                    modifier = Modifier.padding(top = AppSpacing.dp32)
                )
            }
        }
    }
}
