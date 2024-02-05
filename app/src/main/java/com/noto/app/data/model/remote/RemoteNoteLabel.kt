package com.noto.app.data.model.remote

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RemoteNoteLabel(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val noteId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val labelId: UUID,
)