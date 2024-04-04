package com.noto.app.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.noto.app.ui.theme.NotoTheme

fun Modifier.surface() = composed {
    this
        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
        .padding(NotoTheme.dimensions.medium)
}