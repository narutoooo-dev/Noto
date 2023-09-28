package com.noto.app.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

private val CircularProgressIndicatorStrokeWidth = 5.dp
private val ScreenProgressIndicatorSize = 50.dp
private val ActionProgressIndicatorSize = 24.dp
private val DefaultStrokeCap = StrokeCap.Round

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