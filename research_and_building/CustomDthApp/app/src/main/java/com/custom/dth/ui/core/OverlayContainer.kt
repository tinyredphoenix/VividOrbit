package com.custom.dth.ui.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.custom.dth.ui.features.guide.overlays.CategoryPanel
import com.custom.dth.ui.features.settings.SettingsList

/**
 * Root container for presenting Overlays.
 * Subscribes to the OverlayManager state and animates panels in/out.
 */
@Composable
fun OverlayContainer(
    overlayManager: OverlayManager,
    focusManager: TvFocusManager,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Base content (e.g. GuideCoordinator or Playback)
        content()
        
        // HARD RULE: Focus restoration is mandatory.
        // Whenever an overlay closes, the previously focused element MUST be restored exactly.
        LaunchedEffect(overlayManager.topOverlay) {
            if (overlayManager.topOverlay != OverlayType.NONE) {
                // An overlay just opened. 
                // TvFocusManager has already remembered the last guide focus 
                // when the overlay was requested.
            } else {
                // All overlays are closed. Restore exact previous focus.
                focusManager.restoreLastFocus()
            }
        }
        
        // Example: Category Panel slides from left
        AnimatedVisibility(
            visible = overlayManager.isVisible(OverlayType.GUIDE_FLOW_2_CATEGORY),
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            CategoryPanel()
        }
        
        // Example: Settings Panel slides from right
        AnimatedVisibility(
            visible = overlayManager.isVisible(OverlayType.SETTINGS),
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            SettingsList()
        }
    }
}
