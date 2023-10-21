package com.noto.app.data.model.remote

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RemoteFolder(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val title: String,
    val createdAt: Instant,
)