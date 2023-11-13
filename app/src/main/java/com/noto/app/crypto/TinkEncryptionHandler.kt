package com.noto.app.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.TinkJsonProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient

class TinkEncryptionHandler : EncryptionHandler {

    private val androidKeystoreKmsClient by lazy { AndroidKeystoreKmsClient.Builder().build() }
    private val keyStoreAead by lazy { androidKeystoreKmsClient.getAead(KeyStoreUri) }

    init {
        AeadConfig.register()
    }

    override fun generateEncryptedDek(): ByteArray {
        val keysetHandle = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM)
        val encryptedDek = TinkJsonProtoKeysetFormat.serializeEncryptedKeyset(keysetHandle, keyStoreAead, EmptyAssociatedData)
        return encryptedDek.encodeToByteArray() // TODO Decide whether to encode the value or return it as JSON instead.
    }

    override fun encryptData(encryptedDek: ByteArray, data: ByteArray): ByteArray {
        val encryptedDek = encryptedDek.decodeToString()
        val keysetHandle = TinkJsonProtoKeysetFormat.parseEncryptedKeyset(encryptedDek, keyStoreAead, EmptyAssociatedData)
        val aead = keysetHandle.getPrimitive(Aead::class.java)
        return aead.encrypt(data, EmptyAssociatedData)
    }

    override fun decryptData(encryptedDek: ByteArray, encryptedData: ByteArray): ByteArray {
        val encryptedDek = encryptedDek.decodeToString()
        val keysetHandle = TinkJsonProtoKeysetFormat.parseEncryptedKeyset(encryptedDek, keyStoreAead, EmptyAssociatedData)
        val aead = keysetHandle.getPrimitive(Aead::class.java)
        return aead.decrypt(encryptedData, EmptyAssociatedData)
    }

    companion object {
        private val EmptyAssociatedData = ByteArray(0)
        private val KeyStoreUri = AndroidKeystoreKmsClient.PREFIX + KeyStoreManager.KeyEncryptionKeyId
    }

}