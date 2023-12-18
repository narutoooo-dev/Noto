package com.noto.app.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient

class TinkCryptoManager : CryptoManager {

    private val androidKeystoreKmsClient by lazy { AndroidKeystoreKmsClient.Builder().build() }
    private val keyStoreAead by lazy { androidKeystoreKmsClient.getAead(KeyStoreUri) }

    init {
        AeadConfig.register()
    }

    override fun generateKeyset(): String {
        val keysetHandle = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM)
        return TinkJsonProtoKeysetFormat.serializeEncryptedKeyset(keysetHandle, keyStoreAead, EmptyAssociatedData)
    }

    override fun encryptData(keyset: String, data: ByteArray): ByteArray {
        val keysetHandle = TinkJsonProtoKeysetFormat.parseEncryptedKeyset(keyset, keyStoreAead, EmptyAssociatedData)
        val aead = keysetHandle.getPrimitive(Aead::class.java)
        return aead.encrypt(data, EmptyAssociatedData)
    }

    override fun decryptData(keyset: String, encryptedData: ByteArray): ByteArray {
        val keysetHandle = TinkJsonProtoKeysetFormat.parseEncryptedKeyset(keyset, keyStoreAead, EmptyAssociatedData)
        val aead = keysetHandle.getPrimitive(Aead::class.java)
        return aead.decrypt(encryptedData, EmptyAssociatedData)
    }

    companion object {
        private val EmptyAssociatedData = ByteArray(0)
        private val KeyStoreUri = AndroidKeystoreKmsClient.PREFIX + KeyStoreManager.KeyEncryptionKeyId
    }

}