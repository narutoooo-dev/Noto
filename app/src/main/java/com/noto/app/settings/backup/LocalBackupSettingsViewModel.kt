package com.noto.app.settings.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.domain.model.AutoBackupDuration
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.toUiState
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

    private val mutableBackUpState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val backUpState get() = mutableBackUpState.asStateFlow()

    private val mutableRestoreState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val restoreState get() = mutableRestoreState.asStateFlow()

    private val mutableExportState = MutableStateFlow<UiState<Uri>>(UiState.Empty)
    val exportState get() = mutableExportState.asStateFlow()

    private val mutableImportState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val importState get() = mutableImportState.asStateFlow()

    fun updateAutoBackupLocation(autoBackupLocation: Uri) = viewModelScope.launch {
        val decodedUri = autoBackupLocation.toString().let(Uri::decode)
        settingsRepository.updateAutoBackupLocation(decodedUri)
    }

    fun updateAutoBackupDuration(autoBackupDuration: AutoBackupDuration) = viewModelScope.launch {
        settingsRepository.updateAutoBackupDuration(autoBackupDuration)
    }

    fun disableAutoBackup() = viewModelScope.launch {
        settingsRepository.updateAutoBackupLocation(null)
    }

    fun backUp() = viewModelScope.launch {
        val uri = autoBackupLocation.value
        mutableBackUpState.value = UiState.Loading
        mutableBackUpState.value = localBackupHandler.export(uri?.toString(), deleteCurrent = true).toUiState()
    }

    fun restore() = viewModelScope.launch {
        val uri = autoBackupLocation.value
        mutableRestoreState.value = UiState.Loading
        mutableRestoreState.value = localBackupHandler.import(uri?.toString()).toUiState()
    }

    fun export(uri: Uri?) = viewModelScope.launch {
        mutableExportState.value = UiState.Loading
        mutableExportState.value = localBackupHandler.export(uri?.toString(), deleteCurrent = false)
            .map { uri ?: Uri.EMPTY }
            .toUiState()
    }

    fun import(uri: Uri?) = viewModelScope.launch {
        mutableImportState.value = UiState.Loading
        mutableImportState.value = localBackupHandler.import(uri?.toString()).toUiState()
    }

}