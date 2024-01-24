package com.noto.app.crypto

import javax.crypto.SecretKey

interface KeyStoreManager {

    suspend fun storeKey(id: String, key: ByteArray)

    suspend fun getKey(id: String): SecretKey?

    suspend fun deleteKey(id: String)

    companion object {
        const val KeyEncryptionKeyId = "key_encryption_key"
        const val AutoBackupPasscodeId = "auto_backup_passcode"
    }

}