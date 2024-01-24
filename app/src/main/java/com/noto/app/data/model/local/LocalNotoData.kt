package com.noto.app.data.model.local

import kotlinx.serialization.Serializable

@Serializable
data class LocalNotoData(
    val folders: List<LocalFolder>,
    val notes: List<LocalNote>,
    val labels: List<LocalLabel>,
    val noteLabels: List<LocalNoteLabel>,
    val settings: LocalSettingsConfig,
) {
    @Serializable
    data class Encrypted(
        val encryptedContent: ByteArray,
        val encodedParameters: String,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Encrypted

            if (!encryptedContent.contentEquals(other.encryptedContent)) return false
            return encodedParameters == other.encodedParameters
        }

        override fun hashCode(): Int {
            var result = encryptedContent.contentHashCode()
            result = 31 * result + encodedParameters.hashCode()
            return result
        }
    }
}