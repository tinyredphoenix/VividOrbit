package com.custom.dth.ui.features.guide

import com.custom.dth.ui.core.ProgramModel

object GuideFocusEngine {

    /**
     * Resolves the target program when navigating vertically to an adjacent channel.
     * Ensures consistent navigation even when program cells don't perfectly align.
     *
     * @param adjacentPrograms The list of programs on the target channel.
     * @param currentTimeMillis The current horizontal "focus time" (e.g., center of the currently focused program).
     * @return The target ProgramModel to focus, or null if empty.
     */
    fun resolveVerticalFocusTarget(
        adjacentPrograms: List<ProgramModel>,
        currentTimeMillis: Long
    ): ProgramModel? {
        if (adjacentPrograms.isEmpty()) return null

        // Priority 1: Overlapping current horizontal cursor
        val overlapping = adjacentPrograms.find { program ->
            currentTimeMillis >= program.startTimeMillis && currentTimeMillis < program.endTimeMillis
        }
        if (overlapping != null) return overlapping

        // Priority 2: Closest program starting AFTER current time
        val nextProgram = adjacentPrograms
            .filter { it.startTimeMillis >= currentTimeMillis }
            .minByOrNull { it.startTimeMillis }
            
        if (nextProgram != null) return nextProgram

        // Priority 3: Closest program ending BEFORE current time
        val previousProgram = adjacentPrograms
            .filter { it.endTimeMillis <= currentTimeMillis }
            .maxByOrNull { it.endTimeMillis }

        return previousProgram
    }
}
