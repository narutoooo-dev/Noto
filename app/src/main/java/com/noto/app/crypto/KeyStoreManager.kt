package com.noto.app.crypto

interface KeyStoreManager {

    suspend fun storeKEK(key: ByteArray, encodedParameters: String)

    companion object {
        const val KeyEncryptionKeyId = "key_encryption_key"
        const val KeyEncryptionKeyParameters = "key_encryption_key_parameters"
    }

}