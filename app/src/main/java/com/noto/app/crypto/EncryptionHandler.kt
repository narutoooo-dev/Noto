package com.noto.app.crypto

interface EncryptionHandler {

    fun generateEncryptedDek(): ByteArray

    fun encryptData(encryptedDek: ByteArray, data: ByteArray): ByteArray

    fun decryptData(encryptedDek: ByteArray, encryptedData: ByteArray): ByteArray

}