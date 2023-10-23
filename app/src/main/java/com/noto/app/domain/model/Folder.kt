package com.noto.app.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Folder(
    val id: Long = 0L,
    val parentId: Long? = null,
    @Deprecated(
        message = "This shouldn't be used directly. Use folder.getTitle(context) instead.",
        replaceWith = ReplaceWith("folder.getTitle(context)", "import com.noto.app.util.getTitle"),
    )
    val title: String = "",
    val position: Int,
    val color: NotoColor = NotoColor.Gray,
    val creationDate: Instant = Clock.System.now(),
    val layout: Layout = Layout.Linear,
    val notePreviewSize: Int = 15,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isShowNoteCreationDate: Boolean = false,
    val newNoteCursorPosition: NewNoteCursorPosition = NewNoteCursorPosition.Body,
    val sortingType: NoteListSortingType = NoteListSortingType.CreationDate,
    val sortingOrder: SortingOrder = SortingOrder.Descending,
    val grouping: Grouping = Grouping.None,
    val groupingOrder: GroupingOrder = GroupingOrder.Descending,
    val isVaulted: Boolean = false,
    val scrollingPosition: Int = 0,
    val filteringType: FilteringType = FilteringType.Inclusive,
    val openNotesIn: OpenNotesIn = OpenNotesIn.Editor,
    val folders: List<Pair<Folder, Int>> = emptyList(),
) {
    @Suppress("FunctionName")
    companion object {
        const val GeneralFolderId = -1L
        fun GeneralFolder() = Folder(id = GeneralFolderId, position = 0, color = NotoColor.Black)
    }
}