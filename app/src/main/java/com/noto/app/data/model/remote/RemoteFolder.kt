package com.noto.app.data.model.remote

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RemoteFolder(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val keyset: String,
    val encryptedContent: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteFolder

        if (id != other.id) return false
        if (keyset != other.keyset) return false
        return encryptedContent.contentEquals(other.encryptedContent)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + keyset.hashCode()
        result = 31 * result + encryptedContent.contentHashCode()
        return result
    }
}