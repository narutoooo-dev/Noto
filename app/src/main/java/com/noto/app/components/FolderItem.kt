package com.noto.app.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.noto.app.R
import com.noto.app.domain.model.Folder
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor
import com.noto.app.util.getTitle
import com.noto.app.util.isGeneral

private const val FolderNotesCountMaxLines = 1
private const val SelectedFolderColorAlpha = 0.1F
private const val DisabledFolderAlpha = 0.5F

data class FolderItem(
    val folder: Folder,
    val isSelected: Boolean,
    val isEnabled: Boolean,
    val depth: Int,
    val childItems: List<FolderItem>,
)

@Composable
fun FolderItem(item: FolderItem, isShowNotesCount: Boolean, onClick: (FolderItem) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val title = remember(context) { item.folder.getTitle(context) }

    Surface(
        onClick = { onClick(item) },
        shape = MaterialTheme.shapes.small,
        color = item.surfaceColor,
        rippleColor = item.folder.color.toColor(),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.isEnabled) 1F else DisabledFolderAlpha),
        enabled = item.isEnabled,
    ) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(NotoTheme.dimensions.medium)
                .padding(start = NotoTheme.dimensions.medium * item.depth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
        ) {
            Icon(
                painter = item.painter,
                contentDescription = title,
                tint = item.folder.color.toColor(),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1F),
                color = item.folder.color.toColor(),
            )
            if (isShowNotesCount) {
                Text(
                    text = item.folder.notesCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = item.folder.color.toColor(),
                    maxLines = FolderNotesCountMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    item.childItems.forEach { childItem ->
        FolderItem(
            item = childItem,
            isShowNotesCount = isShowNotesCount,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

private val FolderItem.painter: Painter
    @Composable
    get() = when {
        isSelected -> painterResource(id = R.drawable.ic_round_folder_open_24)
        folder.isGeneral -> painterResource(id = R.drawable.ic_round_folder_general_24)
        folder.isPinned -> painterResource(id = R.drawable.ic_round_pinned_folder_24)
        else -> painterResource(id = R.drawable.ic_round_folder_24)
    }

private val FolderItem.surfaceColor: Color
    @Composable
    get() = if (isSelected) {
        folder.color.toColor().copy(alpha = SelectedFolderColorAlpha)
    } else {
        MaterialTheme.colorScheme.background
    }