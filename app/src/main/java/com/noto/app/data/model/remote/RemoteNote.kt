package com.noto.app.data.model.remote

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RemoteNote(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val folderId: UUID,
    val encryptedContent: ByteArray,
    val metaData: MetaData,
) {

    @Serializable
    data class MetaData(val updatedAt: String)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteNote

        if (id != other.id) return false
        if (folderId != other.folderId) return false
        if (!encryptedContent.contentEquals(other.encryptedContent)) return false
        if (metaData != other.metaData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + folderId.hashCode()
        result = 31 * result + encryptedContent.contentHashCode()
        result = 31 * result + metaData.hashCode()
        return result
    }

}