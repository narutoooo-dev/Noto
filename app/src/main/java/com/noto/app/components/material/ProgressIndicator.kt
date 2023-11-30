package com.noto.app.components.material

import  androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.noto.app.theme.NotoTheme

private val CircularProgressIndicatorStrokeWidth = 5.dp
private val ScreenProgressIndicatorSize = 50.dp
private val ActionProgressIndicatorSize = 24.dp
private val DefaultStrokeCap = StrokeCap.Round
private val PagerCircularProgressIndicatorSize = 12.dp
private val PagerCircularProgressIndicatorCount = 5
private val PagerCircularProgressIndicatorSideCount = (PagerCircularProgressIndicatorCount - 1) / 2

@Composable
fun ScreenCircularProgressIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(ScreenProgressIndicatorSize),
            color = color,
            strokeWidth = CircularProgressIndicatorStrokeWidth,
        )
    }
}

@Composable
fun ActionCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surface
) {
    CircularProgressIndicator(
        progress = progress,
        modifier = modifier.size(ActionProgressIndicatorSize),
        color = color,
        strokeWidth = CircularProgressIndicatorStrokeWidth,
        trackColor = trackColor,
        strokeCap = DefaultStrokeCap,
    )
}

@Composable
fun DotProgressIndicator(
    currentItemIndex: Int,
    totalItemCount: Int,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.surface,
) {
    val lazyListState = rememberLazyListState()
    val itemsSpacing = NotoTheme.dimensions.small
    val totalItemsSpacing = itemsSpacing * PagerCircularProgressIndicatorCount
    val totalItemsSize = PagerCircularProgressIndicatorSize * PagerCircularProgressIndicatorCount
    val totalItemsWidth = totalItemsSize + totalItemsSpacing

    LazyRow(
        modifier = modifier.width(totalItemsWidth),
        state = lazyListState,
        horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(totalItemCount) { itemIndex ->
            DotProgressIndicatorItem(
                isSelected = currentItemIndex == itemIndex,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
            )
        }
    }

    LaunchedEffect(key1 = currentItemIndex) {
        val scrollItemIndex = (currentItemIndex - PagerCircularProgressIndicatorSideCount).coerceIn(0, totalItemCount)
        lazyListState.animateScrollToItem(scrollItemIndex)
    }
}

@Composable
private fun DotProgressIndicatorItem(
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
            .size(PagerCircularProgressIndicatorSize)
    )
}