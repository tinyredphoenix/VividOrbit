package com.custom.dth.ui.features.guide.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.components.tvFocus
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

@Composable
fun ChannelManagementOverlay(
    onCloseRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var channelName by remember { mutableStateOf("Star Movies") }
    var channelNumber by remember { mutableStateOf("101") }
    
    val nameFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        nameFocus.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.PrimaryBackground.copy(alpha = 0.8f))
            .onKeyEvent {
                if (it.key == Key.Back || it.key == Key.Escape) {
                    onCloseRequested()
                    true
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .background(AppColors.ElevatedSurface, RoundedCornerShape(8.dp))
                .padding(AppSpacing.dp24)
        ) {
            BasicText("Edit Channel", style = AppTypography.Title.copy(color = AppColors.PrimaryText))
            
            BasicText("Channel Name", style = AppTypography.Caption, modifier = Modifier.padding(top = AppSpacing.dp16))
            BasicTextField(
                value = channelName,
                onValueChange = { channelName = it },
                textStyle = AppTypography.Body.copy(color = AppColors.PrimaryText),
                cursorBrush = SolidColor(AppColors.AccentBlue),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.dp8)
                    .tvFocus()
                    .focusRequester(nameFocus),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.background(AppColors.SecondarySurface, RoundedCornerShape(4.dp)).padding(AppSpacing.dp12)) {
                        innerTextField()
                    }
                }
            )
            
            BasicText("Static Channel Number", style = AppTypography.Caption, modifier = Modifier.padding(top = AppSpacing.dp16))
            BasicTextField(
                value = channelNumber,
                onValueChange = { channelNumber = it },
                textStyle = AppTypography.Body.copy(color = AppColors.PrimaryText),
                cursorBrush = SolidColor(AppColors.AccentBlue),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.dp8)
                    .tvFocus(),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.background(AppColors.SecondarySurface, RoundedCornerShape(4.dp)).padding(AppSpacing.dp12)) {
                        innerTextField()
                    }
                }
            )
            
            Box(
                modifier = Modifier
                    .padding(top = AppSpacing.dp24)
                    .fillMaxWidth()
                    .tvFocus()
                    .background(AppColors.AccentBlue, RoundedCornerShape(4.dp))
                    .padding(AppSpacing.dp12),
                contentAlignment = Alignment.Center
            ) {
                BasicText("Save Changes", style = AppTypography.Body.copy(color = AppColors.PrimaryText))
            }
        }
    }
}
