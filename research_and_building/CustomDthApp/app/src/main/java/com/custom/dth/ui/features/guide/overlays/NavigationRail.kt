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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.components.CenterPivotList
import com.custom.dth.ui.components.FocusableRow
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

/**
 * Screen 1 - Flow 3: Navigation Rail.
 * The application’s primary navigation destinations. Slides in from the left.
 */
@Composable
fun NavigationRail(
    destinations: List<String>,
    onDestinationSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(AppColors.PrimaryBackground.copy(alpha = 0.95f))
            .padding(vertical = AppSpacing.dp24)
    ) {
        CenterPivotList(
            modifier = Modifier.fillMaxHeight()
        ) {
            items(destinations.size) { index ->
                val destination = destinations[index]
                FocusableRow(
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.dp16, vertical = AppSpacing.dp8)
                        .padding(AppSpacing.dp12)
                ) { isFocused ->
                    BasicText(
                        text = destination,
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
