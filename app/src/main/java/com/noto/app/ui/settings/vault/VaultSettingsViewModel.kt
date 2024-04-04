package com.noto.app.ui.settings.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.crypto.key.PasswordBasedKeyGenerator
import com.noto.app.domain.NotoException
import com.noto.app.domain.PropertyConstants
import com.noto.app.domain.VaultTimeout
import com.noto.app.domain.folder.FolderRepository
import com.noto.app.domain.settings.SettingsRepository
import com.noto.app.domain.vault.VaultRepository
import com.noto.app.ui.UiState
import com.noto.app.ui.component.material.TextFieldStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VaultSettingsViewModel(
    private val folderRepository: FolderRepository,
    private val settingsRepository: SettingsRepository,
    private val vaultRepository: VaultRepository,
    private val vaultPasscodeKeyGenerator: PasswordBasedKeyGenerator,
) : ViewModel() {

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
            if (newVaultPasscode.value.length >= PropertyConstants.VaultPasscodeMinLength) {
                if (currentVaultPasscode.value.isNotBlank()) {
                    val currentHashedPasscode = currentVaultPasscode.value
                        .encodeToByteArray()
                        .let(vaultPasscodeKeyGenerator::generateKey)
                        .key
                        .let(vaultPasscodeKeyGenerator::encodeKeyToString)
                    if (vaultPasscode.value == currentHashedPasscode) {
                        mutableVaultPasscodeState.value = UiState.Loading
                        settingsRepository.updateVaultPasscode(
                            newVaultPasscode.value
                                .encodeToByteArray()
                                .let(vaultPasscodeKeyGenerator::generateKey)
                                .key
                                .let(vaultPasscodeKeyGenerator::encodeKeyToString)
                        )
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
        folderRepository.getVaultedFolders().first().forEach { vaultRepository.removeFolderFromVault(it.id) }
        settingsRepository.updateVaultPasscode(passcode = null)
        settingsRepository.updateVaultTimeout(timeout = VaultTimeout.Immediately)
        settingsRepository.updateScheduledVaultTimeout(timeout = null)
        settingsRepository.updateIsBioAuthEnabled(isEnabled = false)
        settingsRepository.updateIsVaultOpen(isOpen = false)
    }

}