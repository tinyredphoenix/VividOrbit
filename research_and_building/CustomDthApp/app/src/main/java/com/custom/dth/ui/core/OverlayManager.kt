package com.custom.dth.ui.core

import androidx.compose.runtime.mutableStateListOf

enum class OverlayType {
    NONE,
    GUIDE_FLOW_2_CATEGORY,
    GUIDE_FLOW_3_NAV_RAIL,
    GUIDE_FLOW_1A_CONTEXT,
    PLAYBACK_OSD,
    NUMERIC_ENTRY,
    SETTINGS,
    SEARCH,
    AUDIO_TRACK_SELECTOR,
    CHANNEL_MANAGEMENT,
    IMPORT_EXPORT
}

/**
 * Central manager for the UI overlay stack.
 * Ensures overlays do not conflict and transitions are smooth.
 */
class OverlayManager {
    private val _stack = mutableStateListOf<OverlayType>()
    val stack: List<OverlayType> get() = _stack
    
    val topOverlay: OverlayType
        get() = _stack.lastOrNull() ?: OverlayType.NONE
        
    fun push(overlay: OverlayType) {
        if (_stack.lastOrNull() != overlay) {
            _stack.add(overlay)
        }
    }
    
    fun pop() {
        if (_stack.isNotEmpty()) {
            _stack.removeLast()
        }
    }
    
    fun clear() {
        _stack.clear()
    }
    
    fun isVisible(overlay: OverlayType): Boolean {
        return _stack.contains(overlay)
    }
}
