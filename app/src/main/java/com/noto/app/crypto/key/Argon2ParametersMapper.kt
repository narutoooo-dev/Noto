package com.noto.app.crypto.key

import org.bouncycastle.crypto.params.Argon2Parameters
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
object Argon2ParametersMapper {

    fun encodeParametersToString(parameters: Argon2Parameters) = buildString {
        with(parameters) {
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
            salt?.let(::encodeSaltToString)?.let(::append)
        }
    }

    fun decodeStringToParameters(encodedParameters: String): Argon2Parameters {
        return with(encodedParameters) {
            val parts = split('$').filterNot { it.isBlank() }
            val builder = when (parts[0]) {
                "argon2d" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_d)
                "argon2i" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_i)
                "argon2id" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                else -> unknownAlgorithm()
            }
            if (parts.size > 3) { // Check if salt is available
                val salt = parts.last().let(::decodeStringToSalt)
                builder.withSalt(salt)
            }
            val version = parts.getIntValueAtIndex(1)
            builder.withVersion(version)
            val options = parts[2].split(',')
            val memory = options.getIntValueAtIndex(0)
            builder.withMemoryAsKB(memory)
            val iterations = options.getIntValueAtIndex(1)
            builder.withIterations(iterations)
            val parallelism = options.getIntValueAtIndex(2)
            builder.withParallelism(parallelism)
            builder.build()
        }
    }

    private fun encodeSaltToString(salt: ByteArray) = Base64.encode(salt)

    private fun decodeStringToSalt(encodedSalt: String) = Base64.decode(encodedSalt)

    private fun List<String>.getIntValueAtIndex(index: Int) = this[index].split('=').last().toInt()

    private fun unknownAlgorithm(): Nothing = error("Unknown Algorithm.")

}