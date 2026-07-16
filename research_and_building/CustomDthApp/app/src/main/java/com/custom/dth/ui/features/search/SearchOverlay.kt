package com.custom.dth.ui.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.custom.dth.ui.components.tvFocus
import com.custom.dth.ui.theme.AppColors
import com.custom.dth.ui.theme.AppSpacing
import com.custom.dth.ui.theme.AppTypography

@Composable
fun SearchOverlay(
    onCloseRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    
    // In a real implementation, we'd collect this from the ChannelRepository
    val dummyResults = remember(query) {
        if (query.isEmpty()) emptyList() else listOf("Result 1 for $query", "Result 2 for $query")
    }

    LaunchedEffect(Unit) {
        searchFocusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.PrimaryBackground.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .background(AppColors.ElevatedSurface, RoundedCornerShape(8.dp))
                .padding(AppSpacing.dp24)
        ) {
            BasicText("Search Channels", style = AppTypography.Title.copy(color = AppColors.PrimaryText))
            
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                textStyle = AppTypography.Body.copy(color = AppColors.PrimaryText),
                cursorBrush = SolidColor(AppColors.AccentBlue),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Close keyboard */ }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.dp16)
                    .tvFocus()
                    .focusRequester(searchFocusRequester)
                    .onKeyEvent {
                        if (it.key == Key.Back || it.key == Key.Escape) {
                            onCloseRequested()
                            true
                        } else {
                            false
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.SecondarySurface, RoundedCornerShape(4.dp))
                            .padding(AppSpacing.dp12)
                    ) {
                        if (query.isEmpty()) {
                            BasicText("Type channel name or number...", style = AppTypography.Body.copy(color = AppColors.SecondaryText))
                        }
                        innerTextField()
                    }
                }
            )
            
            LazyColumn {
                items(dummyResults) { result ->
                    BasicText(
                        text = result,
                        style = AppTypography.Body,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.dp8)
                            .tvFocus()
                            .padding(AppSpacing.dp8)
                    )
                }
            }
        }
    }
}
