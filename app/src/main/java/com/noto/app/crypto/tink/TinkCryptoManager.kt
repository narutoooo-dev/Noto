package com.noto.app.crypto.tink

interface TinkCryptoManager {

    val keysetGenerator: TinkKeysetGenerator

    fun encryptData(keyset: String, data: ByteArray): ByteArray

    fun decryptData(keyset: String, encryptedData: ByteArray): ByteArray

    companion object {
        val EmptyAssociatedData = ByteArray(0)
    }

}