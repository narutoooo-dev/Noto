package com.noto.app.ui.folder

import com.noto.app.domain.note.Note

data class NoteItemModel(
    val note: Note,
    val isSelected: Boolean,
    val selectionOrder: Int = -1,
    val isDragged: Boolean,
) {
    companion object
}