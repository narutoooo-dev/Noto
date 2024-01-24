package com.noto.app.crypto.tink

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TinkEncryptionHandler(val tinkCryptoManager: TinkCryptoManager, val json: Json) {

    inline fun <reified T : Any> encryptItem(keyset: String, item: T): ByteArray {
        val jsonContent = json.encodeToString(item)
        val encodedContent = jsonContent.encodeToByteArray()
        return tinkCryptoManager.encryptData(keyset, encodedContent)
    }

    inline fun <reified T : Any> decryptItem(keyset: String, encryptedItem: ByteArray): T {
        val decryptedContent = tinkCryptoManager.decryptData(keyset, encryptedItem)
        val decodedContent = decryptedContent.decodeToString()
        return json.decodeFromString<T>(decodedContent)
    }

}