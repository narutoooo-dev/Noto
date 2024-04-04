package com.noto.app.ui.component.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.noto.app.R
import com.noto.app.ui.component.material.NotoSurface
import com.noto.app.ui.theme.NotoTheme

const val NoneItemId = 0L

@Composable
fun NoneItem(isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    NotoSurface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
        rippleColor = MaterialTheme.colorScheme.secondary,
    ) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(NotoTheme.dimensions.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_round_none_24),
                contentDescription = stringResource(id = R.string.none),
            )
            Text(
                text = stringResource(id = R.string.none),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1F),
            )
        }
    }
}
