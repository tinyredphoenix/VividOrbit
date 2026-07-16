package com.custom.dth.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Central manager for TV Focus.
 * Responsibilities:
 * - Remember and restore focus during navigation and overlays.
 * - Centralize DPAD interception logic if necessary.
 */
class TvFocusManager(val focusManager: FocusManager) {
    private var lastFocusedRequester: FocusRequester? = null

    fun requestFocus(requester: FocusRequester) {
        try {
            requester.requestFocus()
            lastFocusedRequester = requester
        } catch (e: Exception) {
            // Ignore if unattached
        }
    }
    
    fun clearFocus() {
        focusManager.clearFocus()
    }
    
    fun restoreLastFocus() {
        lastFocusedRequester?.let { requestFocus(it) }
    }
}

@Composable
fun rememberTvFocusManager(): TvFocusManager {
    val focusManager = LocalFocusManager.current
    return remember { TvFocusManager(focusManager) }
}
