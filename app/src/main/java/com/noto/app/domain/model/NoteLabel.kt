package com.noto.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteLabel(
    val id: Long = 0L,
    val noteId: Long,
    val labelId: Long
)