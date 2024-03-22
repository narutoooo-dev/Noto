package com.noto.app.crypto.salt

data object EmptySaltGenerator : SaltGenerator {
    override fun generateSalt(size: Int): ByteArray = ByteArray(size)
}