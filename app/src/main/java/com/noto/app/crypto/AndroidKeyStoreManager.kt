package com.noto.app.crypto

import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import java.security.KeyStore
import java.security.KeyStore.ProtectionParameter
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AndroidKeyStoreManager(private val keyStore: KeyStore) : KeyStoreManager {

    override suspend fun storeKey(id: String, key: ByteArray) {
        val secretKey = SecretKeySpec(key, Algorithm) as SecretKey
        val entry = KeyStore.SecretKeyEntry(secretKey)
        keyStore.setEntry(id, entry, LoadStoreParameter)
    }

    override suspend fun getKey(id: String): SecretKey? {
        return keyStore.getKey(id, null) as? SecretKey?
    }

    override suspend fun deleteKey(id: String) {
        keyStore.deleteEntry(id)
    }

    companion object {
        private const val Algorithm = "AES"

        private val LoadStoreParameter: ProtectionParameter = KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
    }

}