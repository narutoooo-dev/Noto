package com.noto.app.domain.model

data class KeyData(
    val key: ByteArray,
    val encodedParameters: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyData

        if (!key.contentEquals(other.key)) return false
        if (encodedParameters != other.encodedParameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.contentHashCode()
        result = 31 * result + encodedParameters.hashCode()
        return result
    }
}