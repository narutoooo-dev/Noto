package com.noto.app.crypto

interface EncryptionHandler {

    fun generateDEK(): ByteArray

    fun generateIV(): ByteArray

    fun encryptData(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray

    fun decryptData(key: ByteArray, iv: ByteArray, encryptedData: ByteArray): ByteArray

    fun encodeToString(value: ByteArray): String

    fun decodeToByteArray(value: String): ByteArray

}