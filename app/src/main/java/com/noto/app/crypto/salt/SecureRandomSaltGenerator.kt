package com.noto.app.crypto.salt

import java.security.SecureRandom

class SecureRandomSaltGenerator(private val secureRandom: SecureRandom = SecureRandom()) : SaltGenerator {
    override fun generateSalt(size: Int): ByteArray {
        val salt = ByteArray(size)
        secureRandom.nextBytes(salt)
        return salt
    }
}