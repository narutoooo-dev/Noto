package com.noto.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.zIndex

@Composable
fun NotoTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.NotoIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.clip(MaterialTheme.shapes.small),
        tabs = tabs,
    )
}

@Composable
fun NotoLeadingIconTab(selected: Boolean, text: String, painter: Painter, onClick: () -> Unit) {
    LeadingIconTab(
        selected = selected,
        onClick = onClick,
        text = { Text(text = text) },
        icon = { Icon(painter = painter, contentDescription = text) },
        selectedContentColor = MaterialTheme.colorScheme.onPrimary,
        unselectedContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.zIndex(1F),
    )
}

@Composable
fun TabRowDefaults.NotoIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
    )
}