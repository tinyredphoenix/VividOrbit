package com.custom.dth.ui.features.guide.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.components.FocusableRow
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppRadius
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

/**
 * Screen 1 - Flow 1A: Context Actions Panel.
 * The panel is opened only through the application's configured context-action trigger.
 * The trigger implementation is platform-specific, but the resulting behavior must match Flow 1A.
 */
@Composable
fun ContextActionsPanel(
    actions: List<String>,
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(AppRadius.dp8))
            .background(AppColors.ElevatedSurface)
            .padding(AppSpacing.dp8)
    ) {
        actions.forEach { action ->
            FocusableRow(
                onClick = { onActionSelected(action) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.dp4)
                    .padding(AppSpacing.dp12)
            ) { isFocused ->
                BasicText(
                    text = action,
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
