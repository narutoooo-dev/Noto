package com.noto.app.crypto

import android.util.Base64
import com.noto.app.domain.model.KeyData
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom

// TODO: Research values for all number properties. Supabase (Bcrypt) doesn't take more than 72 bytes.
private const val SaltSize = 32 // Bytes
private const val HashSize = 32 // Bytes
private const val KeySize = 32 // Bytes
private const val Parallelism = 1
private const val MemoryInKB = 16_384 // 16 MB
private const val Iterations = 10
private const val Version = Argon2Parameters.ARGON2_VERSION_13
private const val Type = Argon2Parameters.ARGON2_id
private const val EncodingFlags = Base64.NO_PADDING

class Argon2PasswordTransformer(private val secureRandom: SecureRandom = SecureRandom()) : PasswordTransformer {

    private val generator = Argon2BytesGenerator()

    override fun hashPassword(password: ByteArray): KeyData {
        val hash = ByteArray(HashSize)
        val salt = generateSalt()
        val parameters = buildParameters(salt)
        generator.init(parameters)
        generator.generateBytes(password, hash)
        val encodedParameters = parameters.encodeToString()
        return KeyData(hash, encodedParameters)
    }

    override fun verifyPassword(password: ByteArray, encodedParameters: String): ByteArray {
        val hash = ByteArray(HashSize)
        val parameters = encodedParameters.decodeToParameters()
        generator.init(parameters)
        generator.generateBytes(password, hash)
        return hash
    }

    override fun generateKek(password: ByteArray): KeyData {
        val key = ByteArray(KeySize)
        val parameters = buildParameters(salt = null)
        generator.init(parameters)
        generator.generateBytes(password, key)
        return KeyData(key, parameters.encodeToString())
    }

    //On Desktop, use java.util.Base64 (withoutPadding) by using expect /actual mechanism.
    override fun encodeToString(value: ByteArray): String = Base64.encodeToString(value, EncodingFlags)

    override fun decodeToByteArray(value: String): ByteArray = Base64.decode(value, EncodingFlags)

    private fun shouldRehashAgain(parameters: Argon2Parameters): Boolean {
        // Check salt size or not?
        return parameters.salt.size < SaltSize || parameters.memory < MemoryInKB || parameters.iterations < Iterations
    }

    private fun buildParameters(salt: ByteArray?) = Argon2Parameters.Builder(Type)
        .withSalt(salt)
        .withParallelism(Parallelism)
        .withMemoryAsKB(MemoryInKB)
        .withIterations(Iterations)
        .withVersion(Version)
        .build()

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SaltSize)
        secureRandom.nextBytes(salt)
        return salt
    }

    private fun Argon2Parameters.encodeToString() = buildString {
        append("$")
        when (type) {
            Argon2Parameters.ARGON2_d -> append("argon2d")
            Argon2Parameters.ARGON2_i -> append("argon2i")
            Argon2Parameters.ARGON2_id -> append("argon2id")
            else -> unknownAlgorithm()
        }
        append("\$v=")
        append(version)
        append("\$m=")
        append(memory)
        append(",t=")
        append(iterations)
        append(",p=")
        append(lanes)
        append("$")
        salt?.let(this@Argon2PasswordTransformer::encodeToString)?.let(::append)
    }

    private fun String.decodeToParameters(): Argon2Parameters {
        val parts = split('$').filterNot { it.isBlank() }
        val builder = when (parts[0]) {
            "argon2d" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_d)
            "argon2i" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_i)
            "argon2id" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            else -> unknownAlgorithm()
        }
        val salt = parts.last().let(this@Argon2PasswordTransformer::decodeToByteArray)
        builder.withSalt(salt)
        val version = parts.getIntValueAtIndex(1)
        builder.withVersion(version)
        val options = parts[2].split(',')
        val memory = options.getIntValueAtIndex(0)
        builder.withMemoryAsKB(memory)
        val iterations = options.getIntValueAtIndex(1)
        builder.withIterations(iterations)
        val parallelism = options.getIntValueAtIndex(2)
        builder.withParallelism(parallelism)
        return builder.build()
    }

    private fun List<String>.getIntValueAtIndex(index: Int) = this[index].split('=').last().toInt()

    private fun unknownAlgorithm(): Nothing = error("Unknown Algorithm.")
}