package com.noto.app.crypto

import com.noto.app.crypto.key.PasswordBasedKeyGenerator
import com.noto.app.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.first

class VaultEncryptionHandler(
    val settingsRepository: SettingsRepository,
    val vaultPasscodeKeyGenerator: PasswordBasedKeyGenerator,
    val encryptionHandler: RawAesEncryptionHandler,
) {

    suspend inline fun <reified T : Any> encryptItem(item: T): String {
        val vaultPasscode = settingsRepository.vaultPasscode.first()!!
        val key = vaultPasscodeKeyGenerator.decodeStringToKey(vaultPasscode)
        val bytes = encryptionHandler.encryptItem(key, item)
        return RawAesCryptoManager.encodeDataToString(bytes)
    }

    suspend inline fun <reified T : Any> decryptItem(encryptedItem: String): T {
        val vaultPasscode = settingsRepository.vaultPasscode.first()!!
        val key = vaultPasscodeKeyGenerator.decodeStringToKey(vaultPasscode)
        val bytes = RawAesCryptoManager.decodeStringToData(encryptedItem)
        return encryptionHandler.decryptItem<T>(key, bytes)
    }

}