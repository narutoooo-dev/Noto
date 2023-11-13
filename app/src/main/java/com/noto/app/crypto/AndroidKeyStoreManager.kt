package com.noto.app.crypto

import android.os.Build
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import java.security.KeyStore.ProtectionParameter
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AndroidKeyStoreManager(private val keyStore: KeyStore, private val dataStore: DataStore<Preferences>) : KeyStoreManager {

    override suspend fun storeKek(key: ByteArray, encodedParameters: String) {
        val secretKey = SecretKeySpec(key, Algorithm) as SecretKey
        val entry = KeyStore.SecretKeyEntry(secretKey)
        keyStore.setEntry(KeyStoreManager.KeyEncryptionKeyId, entry, LoadStoreParameter)
        dataStore.setEncodedParameters(encodedParameters)
    }

    override suspend fun getKekEncodedParameters(): String {
        return dataStore.data
            .map { it[KekParametersKey].toString() }
            .first()
    }

    private suspend fun DataStore<Preferences>.setEncodedParameters(encodedParameters: String) {
        edit { preferences -> preferences[KekParametersKey] = encodedParameters }
    }

    companion object {
        private const val Algorithm = "AES"
        private val KekParametersKey = stringPreferencesKey(KeyStoreManager.KeyEncryptionKeyParameters)

        @RequiresApi(Build.VERSION_CODES.M)
        private val LoadStoreParameter: ProtectionParameter = KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
    }

}