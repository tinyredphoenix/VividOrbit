package com.custom.dth.ui.features.guide.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.components.CenterPivotList
import com.custom.dth.ui.components.FocusableRow
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

/**
 * Screen 1 - Flow 2: Category Panel.
 * Left-aligned sliding panel for browsing channel groups while keeping the guide visible.
 */
@Composable
fun CategoryPanel(
    categories: List<String>,
    onCategoryFocused: (String) -> Unit,
    onCloseRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Very dark bluish-charcoal background with a subtle vertical gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF111418), // Dark bluish-charcoal
            Color(0xFF0A0C0F)
        )
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp) // Approx 25-28% on 1080p
            .background(backgroundBrush)
            .padding(vertical = AppSpacing.dp24)
    ) {
        CenterPivotList(
            modifier = Modifier.fillMaxHeight()
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                FocusableRow(
                    onClick = onCloseRequested,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.dp16, vertical = AppSpacing.dp8)
                        .padding(AppSpacing.dp12)
                ) { isFocused ->
                    if (isFocused) {
                        // Immediately apply filter when focused (no OK required)
                        onCategoryFocused(category)
                    }
                    
                    BasicText(
                        text = category,
                        style = AppTypography.Body.copy(
                            color = if (isFocused) AppColors.FocusedText else AppColors.PrimaryText
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
