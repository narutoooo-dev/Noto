package com.noto.app.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.noto.app.theme.NotoTheme

private val CircularIndicatorSize = 24.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.surface,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pagerState.pageCount) { page ->
            CircularIndicator(isSelected = pagerState.currentPage == page, selectedColor = selectedColor, unselectedColor = unselectedColor)
        }
    }
}


@Composable
private fun CircularIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.surface,
) {
    val backgroundColor by animateColorAsState(targetValue = if (isSelected) selectedColor else unselectedColor)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor, CircleShape)
            .size(CircularIndicatorSize)
    )
}