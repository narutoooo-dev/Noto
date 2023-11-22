package com.noto.app.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noto.app.R
import com.noto.app.filtered.FilteredItemModel
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor

private const val ItemNotesCountMaxLines = 1
private val BorderStrokeWidth = 1.dp
private const val SelectedItemColorAlpha = 0.1F

@Composable
fun FilteredItem(
    item: FilteredItemModel,
    notesCount: Int,
    isSelected: Boolean,
    isShowNotesCount: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(BorderStrokeWidth, item.color.toColor()),
        rippleColor = item.color.toColor(),
        color = if (isSelected) item.color.toColor().copy(SelectedItemColorAlpha) else MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(NotoTheme.dimensions.medium),
        ) {
            Column(
                modifier = Modifier.weight(1F),
                verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
            ) {
                Icon(painter = item.painter, contentDescription = item.title, tint = item.color.toColor())
                Text(text = item.title, style = MaterialTheme.typography.titleSmall, color = item.color.toColor())
            }
            if (isShowNotesCount) {
                Text(
                    text = notesCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = item.color.toColor(),
                    maxLines = ItemNotesCountMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

    }
}

private val FilteredItemModel.painter: Painter
    @Composable
    get() = when (this) {
        FilteredItemModel.All -> painterResource(id = R.drawable.ic_round_all_notes_24)
        FilteredItemModel.Recent -> painterResource(id = R.drawable.ic_round_schedule_24)
        FilteredItemModel.Scheduled -> painterResource(id = R.drawable.ic_round_notifications_active_24)
        FilteredItemModel.Archived -> painterResource(id = R.drawable.ic_round_inventory_24)
    }

private val FilteredItemModel.title: String
    @Composable
    get() = when (this) {
        FilteredItemModel.All -> stringResource(id = R.string.all)
        FilteredItemModel.Recent -> stringResource(id = R.string.recent)
        FilteredItemModel.Scheduled -> stringResource(id = R.string.scheduled)
        FilteredItemModel.Archived -> stringResource(id = R.string.archived)
    }