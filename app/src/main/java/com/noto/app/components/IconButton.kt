package com.noto.app.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.noto.app.theme.NotoTheme

@Composable
fun FilledIconButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(modifier = Modifier.padding(NotoTheme.dimensions.medium), contentAlignment = Alignment.Center) {
            Icon(painter = painter, contentDescription = contentDescription)
        }
    }
}