package com.custom.dth.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import com.custom.dth.ui.core.UiFeatureFlags
import com.custom.dth.ui.theme.AppAnimation
import com.custom.dth.ui.theme.AppColors

/**
 * The single source of truth for focus animations in the app.
 * Prevents scattered hardcoded values across the codebase.
 */
object FocusAnimator {
    private const val FOCUSED_SCALE = 1.02f
    private const val UNFOCUSED_SCALE = 1.0f

    @Composable
    fun animateScale(isFocused: Boolean): State<Float> {
        return animateFloatAsState(
            targetValue = if (isFocused && UiFeatureFlags.AnimationsEnabled) FOCUSED_SCALE else UNFOCUSED_SCALE,
            animationSpec = tween(durationMillis = AppAnimation.fast),
            label = "focusScale"
        )
    }

    @Composable
    fun animateSurfaceColor(isFocused: Boolean, unfocusedColor: Color = AppColors.SecondarySurface): State<Color> {
        return animateColorAsState(
            targetValue = if (isFocused) AppColors.FocusedSurface else unfocusedColor,
            animationSpec = tween(durationMillis = AppAnimation.fast),
            label = "focusColor"
        )
    }

    @Composable
    fun animateTextColor(isFocused: Boolean, unfocusedColor: Color = AppColors.PrimaryText): State<Color> {
        return animateColorAsState(
            targetValue = if (isFocused) AppColors.FocusedText else unfocusedColor,
            animationSpec = tween(durationMillis = AppAnimation.fast),
            label = "focusTextColor"
        )
    }
}
