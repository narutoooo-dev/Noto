package com.noto.app.crypto

interface CryptoManager {

    fun generateKeyset(): String

    fun encryptData(keyset: String, data: ByteArray): ByteArray

    fun decryptData(keyset: String, encryptedData: ByteArray): ByteArray

}