package com.noto.app.crypto.tink

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient
import com.noto.app.crypto.KeyStoreManager

class TinkAesCryptoManager : TinkCryptoManager {

    private val androidKeystoreKmsClient by lazy { AndroidKeystoreKmsClient.Builder().build() }
    private val keyStoreAead by lazy { androidKeystoreKmsClient.getAead(KeyStoreUri) }

    init {
        AeadConfig.register()
    }

    override val keysetGenerator: TinkKeysetGenerator by lazy {
        object : TinkKeysetGenerator {
            override val keysetEncryptionAead: Aead = keyStoreAead

            override fun generateEncryptedKeyset(): String {
                val keysetHandle = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM)
                return TinkJsonProtoKeysetFormat.serializeEncryptedKeyset(keysetHandle, keyStoreAead, TinkCryptoManager.EmptyAssociatedData)
            }
        }
    }

    override fun encryptData(keyset: String, data: ByteArray): ByteArray {
        val keysetHandle = TinkJsonProtoKeysetFormat.parseEncryptedKeyset(keyset, keyStoreAead, TinkCryptoManager.EmptyAssociatedData)
        val aead = keysetHandle.getPrimitive(Aead::class.java)
        return aead.encrypt(data, TinkCryptoManager.EmptyAssociatedData)
    }

    override fun decryptData(keyset: String, encryptedData: ByteArray): ByteArray {
        val keysetHandle = TinkJsonProtoKeysetFormat.parseEncryptedKeyset(keyset, keyStoreAead, TinkCryptoManager.EmptyAssociatedData)
        val aead = keysetHandle.getPrimitive(Aead::class.java)
        return aead.decrypt(encryptedData, TinkCryptoManager.EmptyAssociatedData)
    }

    companion object {
        private val KeyStoreUri = AndroidKeystoreKmsClient.PREFIX + KeyStoreManager.KeyEncryptionKeyId
    }

}