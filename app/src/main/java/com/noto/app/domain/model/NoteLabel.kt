package com.noto.app.domain.model

data class NoteLabel(
    val id: Long = 0L,
    val noteId: Long,
    val labelId: Long
)