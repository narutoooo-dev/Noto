package com.noto.app.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.TextFieldStatus
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.Constants
import com.noto.app.util.hash
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaultPasscodeViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

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
            val hashedVaultPasscode = vaultPasscode.value.hash()
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
                settingsRepository.updateVaultPasscode(vaultPasscode.value.hash())
                mutableState.value = UiState.Success(Unit)
            } else {
                mutableState.value = UiState.Failure(NotoException.Vault.PasscodeRequirements)
            }
        } else {
            mutableState.value = UiState.Failure(NotoException.Vault.PasscodeIsRequired)
        }
    }

}