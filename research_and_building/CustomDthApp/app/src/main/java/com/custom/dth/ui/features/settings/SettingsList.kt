package com.custom.dth.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.components.CenterPivotList
import com.custom.dth.ui.components.FocusableRow
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

/**
 * Screen 3 - Settings: Flows 1 and 2.
 * Right-side sliding panel.
 * Displays the application's configurable settings.
 * The available categories and options are determined by the application's supported features.
 */
@Composable
fun SettingsList(
    settingsCategories: List<String>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(AppColors.PrimaryBackground.copy(alpha = 0.95f))
            .padding(vertical = AppSpacing.dp24)
    ) {
        CenterPivotList(
            modifier = Modifier.fillMaxHeight()
        ) {
            items(settingsCategories.size) { index ->
                val category = settingsCategories[index]
                FocusableRow(
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.dp16, vertical = AppSpacing.dp8)
                        .padding(AppSpacing.dp12)
                ) { isFocused ->
                    BasicText(
                        text = category,
                        style = AppTypography.Body.copy(
                            color = if (isFocused) AppColors.FocusedText else AppColors.PrimaryText
                        )
                    )
                }
            }
        }
    }
}
