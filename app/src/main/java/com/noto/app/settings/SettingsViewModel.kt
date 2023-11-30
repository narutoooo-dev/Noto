package com.noto.app.settings

import android.app.Application
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.domain.model.*
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.readText
import com.noto.app.util.writeText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val folderRepository: FolderRepository,
    private val settingsRepository: SettingsRepository,
    private val application: Application,
) : AndroidViewModel(application) {

    val userStatus = settingsRepository.userStatus
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserStatus.New)

    val theme = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.Lazily, Theme.System)

    val font = settingsRepository.font
        .stateIn(viewModelScope, SharingStarted.Lazily, Font.Nunito)

    val icon = settingsRepository.icon
        .stateIn(viewModelScope, SharingStarted.Eagerly, Icon.Futuristic)

    val isShowNotesCount = settingsRepository.isShowNotesCount
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val vaultPasscode = settingsRepository.vaultPasscode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val mainInterfaceId = settingsRepository.mainInterfaceId
        .stateIn(viewModelScope, SharingStarted.Eagerly, Folder.GeneralFolderId)

    val isRememberScrollingPosition = settingsRepository.isRememberScrollingPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val quickNoteFolderId = settingsRepository.quickNoteFolderId
        .stateIn(viewModelScope, SharingStarted.Eagerly, Folder.GeneralFolderId)

    val quickExit = settingsRepository.quickExit
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val continuousSearch = settingsRepository.continuousSearch
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val previewAutoScroll = settingsRepository.previewAutoScroll
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val mutableExportState = MutableStateFlow<UiState<Uri>>(UiState.Empty)
    val exportState get() = mutableExportState.asStateFlow()

    private val mutableImportState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val importState get() = mutableImportState.asStateFlow()

    companion object {
        private const val FileName = "Noto.json"
        private const val JsonFileType = "application/json"
        private const val OctetStreamFileType = "application/octet-stream"
        val FileTypes = arrayOf(JsonFileType, OctetStreamFileType)
    }

    fun toggleShowNotesCount() = viewModelScope.launch {
        settingsRepository.updateIsShowNotesCount(!isShowNotesCount.value)
    }

    fun exportData(uri: Uri?) = viewModelScope.launch {
        mutableExportState.value = UiState.Loading
        mutableExportState.value = if (uri != null) {
            val documentFile = DocumentFile.fromTreeUri(application, uri)?.createFile(JsonFileType, FileName)
            if (documentFile != null) {
                val outputStream = application.contentResolver?.openOutputStream(documentFile.uri)
                if (outputStream != null) {
                    val data = settingsRepository.exportNotoData()
                    outputStream.writeText(data)
                    UiState.Success(documentFile.uri)
                } else {
                    UiState.Failure(NotoException.Export.ExportFailed)
                }
            } else {
                UiState.Failure(NotoException.Export.FileCreationFailed)
            }
        } else {
            UiState.Failure(NotoException.Export.NoFolderSelected)
        }
    }

    fun importData(uri: Uri?) = viewModelScope.launch {
        mutableImportState.value = UiState.Loading
        mutableImportState.value = if (uri != null) {
            val inputStream = application.contentResolver?.openInputStream(uri)
            if (inputStream != null) {
                val data = inputStream.readText()
                settingsRepository.importNotoData(data)
                UiState.Success(Unit)
            } else {
                UiState.Failure(NotoException.Import.ImportFailed)
            }
        } else {
            UiState.Failure(NotoException.Import.NoFileSelected)
        }
    }

    fun updateLastVersion() = viewModelScope.launch {
        settingsRepository.updateLastVersion(Release.Version.Current.format())
    }

    fun setMainInterfaceId(folderId: Long) = viewModelScope.launch {
        settingsRepository.updateMainInterfaceId(folderId)
    }

    fun updateTheme(value: Theme) = viewModelScope.launch {
        settingsRepository.updateTheme(value)
    }

    fun updateFont(value: Font) = viewModelScope.launch {
        settingsRepository.updateFont(value)
    }

    fun updateLanguage(value: Language) = viewModelScope.launch {
        settingsRepository.updateLanguage(value)
    }

    fun updateIcon(value: Icon) = viewModelScope.launch {
        settingsRepository.updateIcon(value)
    }

    fun toggleRememberScrollingPosition() = viewModelScope.launch {
        settingsRepository.updateIsRememberScrollingPosition(!isRememberScrollingPosition.value)
    }

    fun toggleQuickExit() = viewModelScope.launch {
        settingsRepository.updateQuickExit(!quickExit.value)
    }

    fun toggleContinuousSearch() = viewModelScope.launch {
        settingsRepository.updateContinuousSearch(!continuousSearch.value)
    }

    fun togglePreviewAutoScroll() = viewModelScope.launch {
        settingsRepository.updatePreviewAutoScroll(!previewAutoScroll.value)
    }

    fun getFolderById(folderId: Long) = folderRepository.getFolderById(folderId)

    fun setQuickNoteFolderId(folderId: Long) = viewModelScope.launch {
        settingsRepository.updateQuickNoteFolderId(folderId)
    }

}