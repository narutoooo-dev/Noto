package com.noto.app.crypto.salt

interface SaltGenerator {

    fun generateSalt(size: Int): ByteArray

}