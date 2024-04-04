package com.noto.app.domain

enum class FilteredItem(val id: Long, val color: NotoColor) {
    All(-2L, NotoColor.Blue),
    Recent(-3L, NotoColor.Yellow),
    Scheduled(-5L, NotoColor.Red),
    Archived(-6L, NotoColor.Purple);

    companion object {
        val Ids = entries.map(FilteredItem::id)
        const val AllFoldersId = -4L
    }

    data class NotesCount(
        val all: Int,
        val recent: Int,
        val scheduled: Int,
        val archived: Int,
    )

}