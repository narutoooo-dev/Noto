package com.noto.app.crypto.key

sealed interface KeyData {

    val key: ByteArray

    data class DefaultKey(override val key: ByteArray) : KeyData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DefaultKey

            return key.contentEquals(other.key)
        }

        override fun hashCode(): Int {
            return key.contentHashCode()
        }
    }

    data class Argon2Key(override val key: ByteArray, val encodedParameters: String) : KeyData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Argon2Key

            if (!key.contentEquals(other.key)) return false
            return encodedParameters == other.encodedParameters
        }

        override fun hashCode(): Int {
            var result = key.contentHashCode()
            result = 31 * result + encodedParameters.hashCode()
            return result
        }
    }

}