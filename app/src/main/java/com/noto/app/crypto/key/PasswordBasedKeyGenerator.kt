package com.noto.app.crypto.key

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
interface PasswordBasedKeyGenerator {

    val keySize: Int

    fun generateKey(password: ByteArray): KeyData

    fun encodeKeyToString(key: ByteArray): String = Base64.encode(key)

    fun decodeStringToKey(encodedKey: String): ByteArray = Base64.decode(encodedKey)

}