package com.custom.dth.ui.core

/**
 * Centralized feature flags for the UI.
 * Replaces scattered `if (DEBUG)` checks.
 */
object UiFeatureFlags {
    // Master toggle for animations
    const val AnimationsEnabled = true
    
    // Toggle for the experimental custom EPG guide
    const val ExperimentalGuide = true
    
    // Shows a debug visual overlay around focused items to ensure no invisible glows exist
    const val DebugFocusOverlay = false
    
    // Visualizes layout bounds in custom layouts (e.g. CenterPivotList, ProgramGrid)
    const val ShowLayoutBounds = false
    
    // Logs or visualizes center-pivot scroll calculations
    const val CenterPivotDebug = false
}
