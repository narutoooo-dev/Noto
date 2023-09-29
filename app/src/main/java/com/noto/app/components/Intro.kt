package com.noto.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun IntroPageTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun IntroPageDescription(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun IntroPageImage(painter: Painter, contentDescription: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(painter = painter, contentDescription = contentDescription)
    }
}