package com.noto.app.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class RawAesCryptoManager(private val secureRandom: SecureRandom = SecureRandom()) {

    fun encryptData(key: SecretKey, iv: ByteArray, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(DataEncryptionAlgorithm) ?: error(DataEncryptionAlgorithmExceptionMessage)
        val ivParameterSpec = IvParameterSpec(iv)
        val parameterSpec = createParameterSpec(ivParameterSpec)
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec, secureRandom)
        return cipher.doFinal(data) ?: error(DataEncryptionExceptionMessage)
    }

    fun encryptData(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, SecretKeyAlgorithm)
        return encryptData(secretKey, iv, data)
    }

    fun decryptData(key: SecretKey, iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(DataEncryptionAlgorithm) ?: error(DataEncryptionAlgorithmExceptionMessage)
        val ivParameterSpec = IvParameterSpec(iv)
        val parameterSpec = createParameterSpec(ivParameterSpec)
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec, secureRandom)
        return cipher.doFinal(encryptedData) ?: error(DataDecryptionExceptionMessage)
    }

    fun decryptData(key: ByteArray, iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, SecretKeyAlgorithm)
        return decryptData(secretKey, iv, encryptedData)
    }

    fun encodeDataToString(value: ByteArray): String = Base64.encode(value)

    fun decodeStringToData(value: String): ByteArray = Base64.decode(value)

    private fun createParameterSpec(ivParameterSpec: IvParameterSpec) = GCMParameterSpec(AuthenticationTagSize, ivParameterSpec.iv)

    companion object {
        private const val DataEncryptionAlgorithm = "AES/GCM/NoPadding"
        private const val AuthenticationTagSize = 128 // Bits
        private const val InitializationVectorSize = 12 // Bytes
        private const val DataEncryptionAlgorithmExceptionMessage = "Data encryption algorithm not found."
        private const val DataEncryptionExceptionMessage = "Data could not be encrypted."
        private const val DataDecryptionExceptionMessage = "Data could not be decrypted."

        const val SecretKeyAlgorithm = "AES"
        val FixedIv = ByteArray(InitializationVectorSize)
    }

}