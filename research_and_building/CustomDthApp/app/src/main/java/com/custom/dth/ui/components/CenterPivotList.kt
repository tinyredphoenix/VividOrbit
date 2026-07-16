package com.custom.dth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyListScope
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.rememberTvLazyListState

/**
 * A custom list wrapper that enforces the hybrid center-pivot scrolling behavior for TV.
 * 
 * SPECIFICATION MATCH:
 * The spec requires a "hybrid" behavior:
 * - When at the edges, focus moves item to item without scrolling.
 * - Once focus reaches the 50% line (center), focus locks to the center and the list scrolls underneath.
 * 
 * Fortunately, TvLazyColumn with PivotOffsets(parentFraction = 0.5f, childFraction = 0.5f) 
 * exactly implements this hybrid model natively. It does NOT "always keep focus centered" 
 * (which would break the top/bottom bounds) nor "always move focus". It perfectly transitions 
 * from edge-movement to center-scrolling as requested.
 */
@Composable
fun CenterPivotList(
    modifier: Modifier = Modifier,
    state: TvLazyListState = rememberTvLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: TvLazyListScope.() -> Unit
) {
    TvLazyColumn(
        modifier = modifier,
        state = state,
        pivotOffsets = PivotOffsets(parentFraction = 0.5f, childFraction = 0.5f),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
