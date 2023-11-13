package com.noto.app.crypto

interface KeyStoreManager {

    suspend fun storeKek(key: ByteArray, encodedParameters: String)

    suspend fun getKekEncodedParameters(): String

    companion object {
        const val KeyEncryptionKeyId = "key_encryption_key"
        const val KeyEncryptionKeyParameters = "key_encryption_key_parameters"
    }

}