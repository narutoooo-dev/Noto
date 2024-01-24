package com.noto.app.crypto.key

import com.noto.app.crypto.salt.SaltGenerator
import com.noto.app.domain.model.KeyData
import org.bouncycastle.crypto.generators.Argon2BytesGenerator

class DefaultArgon2KeyGenerator(
    private val saltGenerator: SaltGenerator,
    override val keySize: Int = DefaultKeySize,
    private val saltSize: Int = DefaultSaltSize,
) : Argon2KeyGenerator {

    override val keyGenerator = Argon2BytesGenerator()

    override fun generateKey(password: ByteArray): KeyData.Argon2Key {
        val key = ByteArray(keySize)
        val salt = if (saltSize != 0) saltGenerator.generateSalt(saltSize) else null
        val parameters = Argon2KeyGenerator.buildArgon2Parameters(salt)
        keyGenerator.init(parameters)
        keyGenerator.generateBytes(password, key)
        val encodedParameters = Argon2ParametersMapper.encodeParametersToString(parameters)
        return KeyData.Argon2Key(key, encodedParameters)
    }

    override fun generateKey(password: ByteArray, encodedParameters: String): KeyData.Argon2Key {
        val key = ByteArray(keySize)
        val parameters = Argon2ParametersMapper.decodeStringToParameters(encodedParameters)
        keyGenerator.init(parameters)
        keyGenerator.generateBytes(password, key)
        return KeyData.Argon2Key(key, encodedParameters)
    }

    companion object {
        const val DefaultKeySize = 32
        const val DefaultSaltSize = 32
    }

}