package com.custom.dth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.custom.dth.ui.theme.AppColors

/**
 * Standard TV Focus Modifier.
 * 
 * Responsibilities:
 * - Handle focus state.
 * - Delegate animations to FocusAnimator.
 * - Apply standard visual treatment (focused background, scale, timing).
 */
fun Modifier.tvFocus(
    interactionSource: MutableInteractionSource? = null,
    unfocusedColor: Color = AppColors.SecondarySurface,
    onFocusChange: ((Boolean) -> Unit)? = null
): Modifier = composed {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by actualInteractionSource.collectIsFocusedAsState()
    
    // Notify focus changes
    if (onFocusChange != null) {
        androidx.compose.runtime.LaunchedEffect(isFocused) {
            onFocusChange(isFocused)
        }
    }

    val scale by FocusAnimator.animateScale(isFocused = isFocused)
    val bgColor by FocusAnimator.animateSurfaceColor(isFocused = isFocused, unfocusedColor = unfocusedColor)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .background(bgColor)
        .focusable(interactionSource = actualInteractionSource)
}
