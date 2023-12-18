package com.noto.app.crypto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EncryptionHandler(val cryptoManager: CryptoManager, val json: Json) {

    inline fun <reified T : Any> encryptItem(keyset: String, item: T): ByteArray {
        val jsonContent = json.encodeToString(item)
        val encodedContent = jsonContent.encodeToByteArray()
        return cryptoManager.encryptData(keyset, encodedContent)
    }


    inline fun <reified T : Any> decryptItem(keyset: String, encryptedItem: ByteArray): T {
        val decryptedContent = cryptoManager.decryptData(keyset, encryptedItem)
        val decodedContent = decryptedContent.decodeToString()
        return json.decodeFromString<T>(decodedContent)
    }

}