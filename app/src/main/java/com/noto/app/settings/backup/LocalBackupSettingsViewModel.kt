package com.noto.app.settings.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.material.TextFieldStatus
import com.noto.app.domain.model.AutoBackupDuration
import com.noto.app.domain.model.BackupFormat
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.toUiState
import com.noto.app.util.Constants
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocalBackupSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val localBackupHandler: LocalBackupHandler,
) : ViewModel() {

    val autoBackupLocation = settingsRepository.autoBackupLocation
        .map { if (it != null) Uri.parse(it) else null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val autoBackupDuration = settingsRepository.autoBackupDuration
        .stateIn(viewModelScope, SharingStarted.Eagerly, AutoBackupDuration.Daily)

    val autoBackupFormat = settingsRepository.autoBackupFormat
        .stateIn(viewModelScope, SharingStarted.Eagerly, BackupFormat.PlainText)

    private val mutableBackUpState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val backUpState get() = mutableBackUpState.asStateFlow()

    private val mutableRestoreState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val restoreState get() = mutableRestoreState.asStateFlow()

    private val mutableExportState = MutableStateFlow<UiState<Uri>>(UiState.Empty)
    val exportState get() = mutableExportState.asStateFlow()

    private val mutableImportState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val importState get() = mutableImportState.asStateFlow()

    private val mutableManualBackupFormat = MutableStateFlow(BackupFormat.PlainText)
    val manualBackupFormat get() = mutableManualBackupFormat.asStateFlow()

    private val mutableBackupPasscode = MutableStateFlow("")
    val backupPasscode get() = mutableBackupPasscode.asStateFlow()

    private val mutableBackupConfirmPasscode = MutableStateFlow("")
    val backupConfirmPasscode get() = mutableBackupConfirmPasscode.asStateFlow()

    private val mutableBackupPasscodeStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val backupPasscodeStatus get() = mutableBackupPasscodeStatus.asStateFlow()

    private val mutableBackupConfirmPasscodeStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val backupConfirmPasscodeStatus get() = mutableBackupConfirmPasscodeStatus.asStateFlow()

    private val mutableBackupPasscodeState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val backupPasscodeState get() = mutableBackupPasscodeState.asStateFlow()

    fun updateAutoBackupLocation(autoBackupLocation: Uri) = viewModelScope.launch {
        val decodedUri = autoBackupLocation.toString().let(Uri::decode)
        settingsRepository.updateAutoBackupLocation(decodedUri)
    }

    fun updateAutoBackupDuration(autoBackupDuration: AutoBackupDuration) = viewModelScope.launch {
        settingsRepository.updateAutoBackupDuration(autoBackupDuration)
    }

    fun updateAutoBackupFormat(autoBackupFormat: BackupFormat) = viewModelScope.launch {
        if (autoBackupFormat == BackupFormat.PlainText) settingsRepository.updateAutoBackupPasscode(passcode = null)
        settingsRepository.updateAutoBackupFormat(autoBackupFormat)
    }

    fun updateAutoBackupPasscode(passcode: String) = viewModelScope.launch {
        settingsRepository.updateAutoBackupPasscode(passcode)
    }

    fun disableAutoBackup() = viewModelScope.launch {
        settingsRepository.updateAutoBackupLocation(null)
    }

    fun updateManualBackupFormat(manualBackupFormat: BackupFormat) {
        mutableManualBackupFormat.value = manualBackupFormat
    }

    fun backUp() = viewModelScope.launch {
        mutableBackUpState.value = UiState.Loading
        val uri = autoBackupLocation.value
        val result = when (autoBackupFormat.value) {
            BackupFormat.PlainText -> settingsRepository.exportNotoData()
            BackupFormat.Encrypted -> settingsRepository.exportEncryptedNotoData()
        }
        mutableBackUpState.value = result.fold(
            onSuccess = { exportedData -> localBackupHandler.export(uri?.toString(), exportedData, deleteCurrent = true).toUiState() },
            onFailure = { UiState.Failure(NotoException.Unknown(it.message)) }
        )
    }

    fun restore() = viewModelScope.launch {
        val uri = autoBackupLocation.value
        val format = autoBackupFormat.value
        mutableRestoreState.value = UiState.Loading
        mutableRestoreState.value = localBackupHandler.import(uri?.toString())
            .mapCatching { data ->
                when (format) {
                    BackupFormat.PlainText -> settingsRepository.importNotoData(data).getOrThrow()
                    BackupFormat.Encrypted -> settingsRepository.importEncryptedNotoData(data).getOrThrow()
                }
            }
            .toUiState()
    }

    fun export(uri: Uri?) = viewModelScope.launch {
        val format = manualBackupFormat.value
        val passcode = backupPasscode.value.takeIf { it.isNotBlank() }
        mutableExportState.value = UiState.Loading
        mutableExportState.value = when (format) {
            BackupFormat.PlainText -> {
                settingsRepository.exportNotoData()
                    .fold(
                        onSuccess = { exportedData ->
                            localBackupHandler.export(uri?.toString(), exportedData, deleteCurrent = false)
                                .map { uri ?: Uri.EMPTY }
                                .toUiState()
                        },
                        onFailure = { UiState.Failure(NotoException.Unknown(it.message)) }
                    )
            }

            BackupFormat.Encrypted -> {
                if (passcode == null) {
                    UiState.Failure(NotoException.LocalBackup.MissingPasscode)
                } else {
                    settingsRepository.exportEncryptedNotoData(passcode)
                        .fold(
                            onSuccess = { exportedData ->
                                localBackupHandler.export(uri?.toString(), exportedData, deleteCurrent = false)
                                    .map { uri ?: Uri.EMPTY }
                                    .toUiState()
                            },
                            onFailure = { UiState.Failure(NotoException.Unknown(it.message)) }
                        )
                }
            }
        }
    }

    fun import(uri: Uri?) = viewModelScope.launch {
        val format = manualBackupFormat.value
        val passcode = backupPasscode.value.takeIf { it.isNotBlank() }
        mutableImportState.value = UiState.Loading
        mutableImportState.value = localBackupHandler.import(uri?.toString())
            .mapCatching { data ->
                when (format) {
                    BackupFormat.PlainText -> settingsRepository.importNotoData(data).getOrThrow()
                    BackupFormat.Encrypted -> {
                        if (passcode == null) {
                            NotoException.LocalBackup.MissingPasscode()
                        } else {
                            settingsRepository.importEncryptedNotoData(data, passcode).getOrThrow()
                        }
                    }
                }
            }
            .toUiState()
    }

    fun setBackupPasscode(passcode: String) {
        mutableBackupPasscode.value = passcode
        if (passcode.isNotBlank()) {
            val isPasscodeValid = Constants.Regex.matchesBackupPasscode(passcode)
            if (isPasscodeValid) {
                mutableBackupPasscodeStatus.value = TextFieldStatus.Empty
            } else if (backupPasscodeStatus.value.isError) {
                mutableBackupPasscodeStatus.value = TextFieldStatus.Error(R.string.backup_passcode_invalid_requirements)
            }
            if (backupConfirmPasscode.value.isNotBlank()) {
                val isConfirmPasscodeValid = backupConfirmPasscode.value == passcode
                if (isConfirmPasscodeValid) {
                    mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Empty
                } else {
                    mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Error(R.string.confirm_passcode_is_invalid)
                }
            }
        } else {
            mutableBackupPasscodeStatus.value = TextFieldStatus.Error(R.string.passcode_is_required)
        }
    }

    fun setBackupConfirmPasscode(confirmPasscode: String) {
        mutableBackupConfirmPasscode.value = confirmPasscode
        if (confirmPasscode.isNotBlank()) {
            val isConfirmPasscodeValid = confirmPasscode == backupPasscode.value
            if (isConfirmPasscodeValid) {
                mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Empty
            } else if (backupConfirmPasscodeStatus.value.isError) {
                mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Error(R.string.confirm_passcode_is_invalid)
            }
        } else {
            mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Error(R.string.confirm_passcode_is_required)
        }
    }

    fun validatePasscode() {
        if (backupPasscode.value.isNotBlank()) {
            val isPasscodeValid = Constants.Regex.matchesBackupPasscode(backupPasscode.value)
            if (!isPasscodeValid) mutableBackupPasscodeStatus.value = TextFieldStatus.Error(R.string.backup_passcode_invalid_requirements)
        }
    }

    fun validateConfirmPasscode() {
        if (backupConfirmPasscode.value.isNotBlank()) {
            val isConfirmPasscodeValid = backupConfirmPasscode.value == backupPasscode.value
            if (!isConfirmPasscodeValid) mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Error(R.string.confirm_passcode_is_invalid)
        }
    }

    fun confirmBackupPasscode() = viewModelScope.launch {
        val isInputValid = checkIsInputValid(backupPasscode.value, backupConfirmPasscode.value)
        if (isInputValid) {
            mutableBackupPasscodeStatus.value = TextFieldStatus.Empty
            mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Empty
            mutableBackupPasscodeState.value = UiState.Success(Unit)
        }
    }

    fun updateManualBackupPasscode(passcode: String) {
        mutableBackupPasscode.value = passcode
    }

    private fun checkIsInputValid(passcode: String, confirmPasscode: String): Boolean {
        val isPasscodeValid = Constants.Regex.matchesBackupPasscode(passcode)
        val isConfirmPasscodeValid = confirmPasscode == passcode

        if (passcode.isBlank()) {
            mutableBackupPasscodeStatus.value = TextFieldStatus.Error(R.string.passcode_is_required)
        } else {
            if (!isPasscodeValid) mutableBackupPasscodeStatus.value = TextFieldStatus.Error(R.string.backup_passcode_invalid_requirements)
        }

        if (confirmPasscode.isBlank()) {
            mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Error(R.string.confirm_passcode_is_required)
        } else {
            if (!isConfirmPasscodeValid) mutableBackupConfirmPasscodeStatus.value = TextFieldStatus.Error(R.string.confirm_passcode_is_invalid)
        }

        return isPasscodeValid && isConfirmPasscodeValid
    }

}