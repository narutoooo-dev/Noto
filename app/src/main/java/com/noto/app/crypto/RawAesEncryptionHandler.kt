package com.noto.app.crypto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RawAesEncryptionHandler(val rawAesCryptoManager: RawAesCryptoManager, val json: Json) {

    inline fun <reified T : Any> encryptItem(key: ByteArray, item: T): ByteArray {
        val jsonContent = json.encodeToString(item)
        val encodedContent = jsonContent.encodeToByteArray()
        return rawAesCryptoManager.encryptData(key, RawAesCryptoManager.FixedIv, encodedContent)
    }

    inline fun <reified T : Any> decryptItem(key: ByteArray, encryptedItem: ByteArray): T {
        val decryptedContent = rawAesCryptoManager.decryptData(key, RawAesCryptoManager.FixedIv, encryptedItem)
        val decodedContent = decryptedContent.decodeToString()
        return json.decodeFromString<T>(decodedContent)
    }

}