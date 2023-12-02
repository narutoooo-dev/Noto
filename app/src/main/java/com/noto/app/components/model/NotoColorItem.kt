package com.noto.app.components.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.noto.app.R
import com.noto.app.domain.model.NotoColor
import com.noto.app.theme.toColor
import com.noto.app.util.DefaultAnimationDuration

private val NotoColorItemSize = 50.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotoColorItem(notoColor: NotoColor, isSelected: Boolean, onClick: (NotoColor) -> Unit, modifier: Modifier = Modifier) {
    val color = notoColor.toColor()
    AnimatedContent(
        targetState = isSelected,
        label = "NotoColorItem",
        contentAlignment = Alignment.Center,
        transitionSpec = { fadeIn(tween(DefaultAnimationDuration.toInt())) togetherWith fadeOut(tween(DefaultAnimationDuration.toInt())) }
    ) { animatedIsSelected ->
        val painter = if (animatedIsSelected)
            painterResource(id = R.drawable.ic_round_check_circle_outline_24)
        else
            painterResource(id = R.drawable.ic_round_circle_24)

        PlainTooltipBox(
            tooltip = { Text(text = notoColor.name) },
            containerColor = color,
        ) {
            Icon(
                painter = painter,
                contentDescription = notoColor.name,
                modifier = modifier
                    .tooltipAnchor()
                    .clip(CircleShape)
                    .clickable { onClick(notoColor) }
                    .size(NotoColorItemSize),
                tint = color,
            )
        }
    }
}