package com.noto.app.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val DataEncryptionAlgorithm = "AES/GCM/NoPadding"
private const val SecretKeyAlgorithm = "AES"
private const val SecretKeySize = 256 // Bits
private const val AuthenticationTagSize = 128 // Bits
private const val InitializationVectorSize = 12 // Bytes
private const val EncodingFlags = 0

object EncryptionHandlerImpl : EncryptionHandler {

    private val secureRandom = SecureRandom()

    override fun generateDEK(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance(SecretKeyAlgorithm)
        keyGenerator?.init(SecretKeySize, secureRandom)
        val key = keyGenerator?.generateKey()?.encoded
        return key ?: error(SecretKeyExceptionMessage)
    }

    override fun generateIV(): ByteArray {
        val iv = ByteArray(InitializationVectorSize)
        secureRandom.nextBytes(iv)
        return iv
    }

    override fun encryptData(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(DataEncryptionAlgorithm)
        val secretKey = SecretKeySpec(key, SecretKeyAlgorithm)
        val ivParameterSpec = IvParameterSpec(iv)
        val parameterSpec = createParameterSpec(ivParameterSpec)
        cipher?.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec, secureRandom)
        return cipher?.doFinal(data) ?: error(EncryptionExceptionMessage)
    }

    override fun decryptData(key: ByteArray, iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(DataEncryptionAlgorithm)
        val secretKey = SecretKeySpec(key, SecretKeyAlgorithm)
        val ivParameterSpec = IvParameterSpec(iv)
        val parameterSpec = createParameterSpec(ivParameterSpec)
        cipher?.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec, secureRandom)
        return cipher.doFinal(encryptedData) ?: error(DecryptionExceptionMessage)
    }

    override fun encodeToString(value: ByteArray): String = Base64.encodeToString(value, EncodingFlags)

    override fun decodeToByteArray(value: String): ByteArray = Base64.decode(value, EncodingFlags)

    private fun createParameterSpec(ivParameterSpec: IvParameterSpec) = GCMParameterSpec(AuthenticationTagSize, ivParameterSpec.iv)
}

private const val SecretKeyExceptionMessage = "Secret key could not be generated."
private const val EncryptionExceptionMessage = "Data could not be encrypted."
private const val DecryptionExceptionMessage = "Data could not be decrypted."