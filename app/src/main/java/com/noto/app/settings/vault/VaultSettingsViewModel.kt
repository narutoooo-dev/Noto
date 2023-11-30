package com.noto.app.settings.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.TextFieldStatus
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.model.VaultTimeout
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.Constants
import com.noto.app.util.hash
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VaultSettingsViewModel(private val folderRepository: FolderRepository, private val settingsRepository: SettingsRepository) : ViewModel() {

    private val mutableVaultPasscodeState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val vaultPasscodeState get() = mutableVaultPasscodeState.asStateFlow()

    private val mutableCurrentVaultPasscode = MutableStateFlow("")
    val currentVaultPasscode get() = mutableCurrentVaultPasscode.asStateFlow()

    private val mutableCurrentVaultPasscodeStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val currentVaultPasscodeStatus get() = mutableCurrentVaultPasscodeStatus.asStateFlow()

    private val mutableNewVaultPasscode = MutableStateFlow("")
    val newVaultPasscode get() = mutableNewVaultPasscode.asStateFlow()

    private val mutableNewVaultPasscodeStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val newVaultPasscodeStatus get() = mutableNewVaultPasscodeStatus.asStateFlow()

    private val vaultPasscode = settingsRepository.vaultPasscode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val vaultTimeout = settingsRepository.vaultTimeout
        .stateIn(viewModelScope, SharingStarted.Lazily, VaultTimeout.Immediately)

    val isBioAuthEnabled = settingsRepository.isBioAuthEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setCurrentVaultPasscode(passcode: String) {
        mutableCurrentVaultPasscode.value = passcode
        if (passcode.isBlank()) {
            setCurrentVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_is_required))
        } else {
            setCurrentVaultPasscodeStatus(TextFieldStatus.Empty)
        }
    }

    fun setCurrentVaultPasscodeStatus(status: TextFieldStatus) {
        mutableCurrentVaultPasscodeStatus.value = status
    }

    fun setNewVaultPasscode(passcode: String) {
        mutableNewVaultPasscode.value = passcode
        if (passcode.isBlank()) {
            setNewVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_is_required))
        } else {
            setNewVaultPasscodeStatus(TextFieldStatus.Empty)
        }
    }

    fun setNewVaultPasscodeStatus(status: TextFieldStatus) {
        mutableNewVaultPasscodeStatus.value = status
    }

    fun updateVaultPasscode() = viewModelScope.launch {
        if (newVaultPasscode.value.isNotBlank()) {
            if (newVaultPasscode.value.length >= Constants.VaultPasscodeMinLength) {
                if (currentVaultPasscode.value.isNotBlank()) {
                    val currentHashedPasscode = currentVaultPasscode.value.hash()
                    if (vaultPasscode.value == currentHashedPasscode) {
                        mutableVaultPasscodeState.value = UiState.Loading
                        settingsRepository.updateVaultPasscode(newVaultPasscode.value.hash())
                        mutableVaultPasscodeState.value = UiState.Success(Unit)
                    } else {
                        mutableVaultPasscodeState.value = UiState.Failure(NotoException.Vault.MismatchedPasscodes)
                    }
                } else {
                    mutableVaultPasscodeState.value = UiState.Failure(NotoException.Vault.PasscodeIsRequired)
                }
            } else {
                mutableVaultPasscodeState.value = UiState.Failure(NotoException.Vault.PasscodeRequirements)
            }
        } else {
            mutableVaultPasscodeState.value = UiState.Failure(NotoException.Vault.NewPasscodeIsRequired)
        }
    }

    fun toggleIsBioAuthEnabled() = viewModelScope.launch {
        settingsRepository.updateIsBioAuthEnabled(!isBioAuthEnabled.value)
    }

    fun updateVaultTimeout(timeout: VaultTimeout) = viewModelScope.launch {
        settingsRepository.updateVaultTimeout(timeout)
    }

    fun disableVault() = viewModelScope.launch {
        folderRepository.getVaultedFolders().first()
            .map { it.copy(isVaulted = false) }
            .forEach { folderRepository.updateFolder(it) }
        settingsRepository.updateVaultPasscode(passcode = null)
        settingsRepository.updateVaultTimeout(timeout = VaultTimeout.Immediately)
        settingsRepository.updateScheduledVaultTimeout(timeout = null)
        settingsRepository.updateIsBioAuthEnabled(isEnabled = false)
        settingsRepository.updateIsVaultOpen(isOpen = false)
    }

}