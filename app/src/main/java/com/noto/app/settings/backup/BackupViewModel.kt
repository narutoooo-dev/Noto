package com.noto.app.settings.backup

import android.app.Application
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.readText
import com.noto.app.util.writeText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupViewModel(
    private val settingsRepository: SettingsRepository,
    private val application: Application,
) : AndroidViewModel(application) {

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

}