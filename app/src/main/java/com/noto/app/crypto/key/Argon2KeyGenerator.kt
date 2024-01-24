package com.noto.app.crypto.key

import com.noto.app.domain.model.KeyData
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

interface Argon2KeyGenerator : PasswordBasedKeyGenerator {

    val keyGenerator: Argon2BytesGenerator

    override fun generateKey(password: ByteArray): KeyData.Argon2Key

    fun generateKey(password: ByteArray, encodedParameters: String): KeyData.Argon2Key

    companion object {
        private const val DefaultParallelism = 1
        private const val DefaultMemoryInKB = 16_384 // 16 MB
        private const val DefaultIterations = 10
        private const val DefaultVersion = Argon2Parameters.ARGON2_VERSION_13
        private const val DefaultType = Argon2Parameters.ARGON2_id

        fun buildArgon2Parameters(
            salt: ByteArray? = null,
            parallelism: Int = DefaultParallelism,
            memoryInKB: Int = DefaultMemoryInKB,
            iterations: Int = DefaultIterations,
        ) = Argon2Parameters.Builder(DefaultType)
            .withSalt(salt)
            .withParallelism(parallelism)
            .withMemoryAsKB(memoryInKB)
            .withIterations(iterations)
            .withVersion(DefaultVersion)
            .build()


        private fun shouldRehashAgain(saltSize: Int, parameters: Argon2Parameters): Boolean {
            // Check salt size or not?
            // TODO
            return parameters.salt.size < saltSize || parameters.memory < DefaultMemoryInKB || parameters.iterations < DefaultIterations
        }

    }

}