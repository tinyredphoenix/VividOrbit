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
 * A custom list wrapper that automatically handles center-pivot scrolling behavior for TV.
 * When the user scrolls with DPAD, the focus cursor moves towards the center of the list.
 * Once it hits the center, the cursor remains fixed and the content scrolls beneath it.
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
