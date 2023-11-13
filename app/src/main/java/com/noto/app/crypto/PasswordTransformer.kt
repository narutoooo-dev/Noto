package com.noto.app.crypto

import com.noto.app.domain.model.KeyData

interface PasswordTransformer {

    fun hashPassword(password: ByteArray): KeyData

    fun verifyPassword(password: ByteArray, encodedParameters: String): ByteArray

    fun generateKek(password: ByteArray): KeyData

    fun encodeToString(value: ByteArray): String

    fun decodeToByteArray(value: String): ByteArray

}