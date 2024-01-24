package com.noto.app.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.material.TextFieldStatus
import com.noto.app.crypto.key.PasswordBasedKeyGenerator
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaultPasscodeViewModel(
    private val settingsRepository: SettingsRepository,
    private val vaultPasscodeKeyGenerator: PasswordBasedKeyGenerator,
) : ViewModel() {

    private val mutableState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val state get() = mutableState.asStateFlow()

    private val mutableVaultPasscode = MutableStateFlow("")
    val vaultPasscode get() = mutableVaultPasscode.asStateFlow()

    private val mutableVaultPasscodeStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val vaultPasscodeStatus get() = mutableVaultPasscodeStatus.asStateFlow()

    private val currentVaultPasscode = settingsRepository.vaultPasscode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isBioAuthEnabled = settingsRepository.isBioAuthEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setVaultPasscode(passcode: String) {
        mutableVaultPasscode.value = passcode
        if (passcode.isBlank()) {
            setVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_is_required))
        } else {
            setVaultPasscodeStatus(TextFieldStatus.Empty)
        }
    }

    fun setVaultPasscodeStatus(status: TextFieldStatus) {
        mutableVaultPasscodeStatus.value = status
    }

    fun validatePasscode() {
        if (vaultPasscode.value.isNotBlank()) {
            mutableState.value = UiState.Loading
            val hashedVaultPasscode = vaultPasscode.value
                .encodeToByteArray()
                .let(vaultPasscodeKeyGenerator::generateKey)
                .key
                .let(vaultPasscodeKeyGenerator::encodeKeyToString)
            if (hashedVaultPasscode == currentVaultPasscode.value) {
                mutableState.value = UiState.Success(Unit)
            } else {
                mutableState.value = UiState.Failure(NotoException.Vault.InvalidPasscode)
            }
        } else {
            mutableState.value = UiState.Failure(NotoException.Vault.PasscodeIsRequired)
        }
    }

    fun enableVault() = viewModelScope.launch {
        if (vaultPasscode.value.isNotBlank()) {
            if (vaultPasscode.value.length >= Constants.VaultPasscodeMinLength) {
                mutableState.value = UiState.Loading
                settingsRepository.updateVaultPasscode(
                    vaultPasscode.value
                        .encodeToByteArray()
                        .let(vaultPasscodeKeyGenerator::generateKey)
                        .key
                        .let(vaultPasscodeKeyGenerator::encodeKeyToString)
                )
                mutableState.value = UiState.Success(Unit)
            } else {
                mutableState.value = UiState.Failure(NotoException.Vault.PasscodeRequirements)
            }
        } else {
            mutableState.value = UiState.Failure(NotoException.Vault.PasscodeIsRequired)
        }
    }

}