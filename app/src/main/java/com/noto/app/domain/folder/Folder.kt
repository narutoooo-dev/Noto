package com.noto.app.domain.folder

import com.noto.app.domain.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Folder(
    val id: Long,
    val parentFolder: Folder?,
    @Deprecated(
        message = "This shouldn't be used directly. Use folder.getTitle(context) instead.",
        replaceWith = ReplaceWith("folder.getTitle(context)", "import com.noto.app.ui.util.getTitle"),
    )
    val title: String,
    val position: Int,
    val color: NotoColor,
    val creationDate: Instant,
    val layout: Layout,
    val notePreviewSize: Int,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val isShowNoteCreationDate: Boolean,
    val newNoteCursorPosition: NewNoteCursorPosition,
    val sortingType: NoteListSortingType,
    val sortingOrder: SortingOrder,
    val grouping: Grouping,
    val groupingOrder: GroupingOrder,
    val isVaulted: Boolean,
    val scrollingPosition: Int,
    val filteringType: FilteringType,
    val openNotesIn: OpenNotesIn,
    val childFolders: List<Folder>,
    val notesCount: Int,
) {
    @Suppress("FunctionName")
    companion object {
        val Default = Folder(
            id = 0L,
            parentFolder = null,
            title = "",
            position = 0,
            color = NotoColor.Gray,
            creationDate = Clock.System.now(),
            layout = Layout.Linear,
            notePreviewSize = 15,
            isArchived = false,
            isPinned = false,
            isShowNoteCreationDate = false,
            newNoteCursorPosition = NewNoteCursorPosition.Body,
            sortingType = NoteListSortingType.CreationDate,
            sortingOrder = SortingOrder.Descending,
            grouping = Grouping.None,
            groupingOrder = GroupingOrder.Descending,
            isVaulted = false,
            scrollingPosition = 0,
            filteringType = FilteringType.Inclusive,
            openNotesIn = OpenNotesIn.Editor,
            childFolders = emptyList(),
            notesCount = 0,
        )
        const val GeneralFolderId = -1L
        val General = Default.copy(id = GeneralFolderId, color = NotoColor.Black)
    }
}
