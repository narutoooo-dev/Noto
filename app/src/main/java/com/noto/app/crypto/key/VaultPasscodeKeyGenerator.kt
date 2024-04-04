package com.noto.app.crypto.key

import android.util.Base64
import com.noto.app.crypto.salt.SaltGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class VaultPasscodeKeyGenerator(private val saltGenerator: SaltGenerator) : PasswordBasedKeyGenerator {

    private val factory = SecretKeyFactory.getInstance(HashAlgorithm)

    override val keySize: Int = KeySize

    override fun generateKey(password: ByteArray): KeyData {
        val salt = saltGenerator.generateSalt(SaltSize)
        val charArray = password.decodeToString().toCharArray()
        val spec = PBEKeySpec(charArray, salt, HashIterationCount, keySize)
        val key = factory.generateSecret(spec).encoded
        return KeyData.DefaultKey(key)
    }

    companion object {
        private const val HashAlgorithm = "PBKDF2WithHmacSHA1"
        private const val HashIterationCount = 65536
        private const val SaltSize = 16
        private const val KeySize = 128
    }

    override fun encodeKeyToString(key: ByteArray): String = Base64.encodeToString(key, Base64.DEFAULT)

    override fun decodeStringToKey(encodedKey: String): ByteArray = Base64.decode(encodedKey, Base64.DEFAULT)

}