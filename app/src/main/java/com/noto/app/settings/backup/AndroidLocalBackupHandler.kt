package com.noto.app.settings.backup

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class AndroidLocalBackupHandler(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) : LocalBackupHandler {

    override fun validateUri(uri: String?): Boolean {
        return if (uri != null) {
            val androidUri = Uri.parse(uri)
            val isDocumentUri = DocumentFile.isDocumentUri(application, androidUri)
            val documentFile = if (isDocumentUri) {
                DocumentFile.fromSingleUri(application, androidUri)
            } else {
                DocumentFile.fromTreeUri(application, androidUri)
            }
            documentFile?.exists() ?: false
        } else {
            false
        }
    }

    @SuppressLint("Recycle")
    override suspend fun export(uri: String?, deleteCurrent: Boolean): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val androidUri = uri?.let(Uri::parse)
            if (androidUri != null) {
                val documentTree = DocumentFile.fromTreeUri(application, androidUri)
                if (deleteCurrent) documentTree?.findFile(LocalBackupHandler.FileName)?.renameTo(LocalBackupHandler.OldFileName)
                val documentFile = documentTree?.createFile(LocalBackupHandler.JsonFileType, LocalBackupHandler.FileName)
                if (documentFile != null) {
                    val outputStream = application.contentResolver?.openOutputStream(documentFile.uri)
                    if (outputStream != null) {
                        val data = settingsRepository.exportNotoData()
                        outputStream.writeText(data)
                        if (deleteCurrent) documentTree.findFile(LocalBackupHandler.OldFileName)?.delete()
                    } else {
                        if (deleteCurrent) {
                            documentTree.findFile(LocalBackupHandler.FileName)?.delete()
                            documentTree.findFile(LocalBackupHandler.OldFileName)?.renameTo(LocalBackupHandler.FileName)
                        }
                        NotoException.LocalBackup.Export.ExportFailed()
                    }
                } else {
                    if (deleteCurrent) documentTree?.findFile(LocalBackupHandler.OldFileName)?.renameTo(LocalBackupHandler.FileName)
                    NotoException.LocalBackup.Export.FileCreationFailed()
                }
            } else {
                NotoException.LocalBackup.Export.NoFolderSelected()
            }
        }
    }

    @SuppressLint("Recycle")
    override suspend fun import(uri: String?): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val androidUri = uri?.let(Uri::parse)?.toDocumentUri()
            if (androidUri != null) {
                val inputStream = application.contentResolver?.openInputStream(androidUri)
                if (inputStream != null) {
                    val data = inputStream.readText()
                    settingsRepository.importNotoData(data)
                } else {
                    NotoException.LocalBackup.Import.ImportFailed()
                }
            } else {
                NotoException.LocalBackup.Import.NoFileSelected()
            }
        }
    }

    private fun Uri.toDocumentUri(): Uri? {
        val isDocumentUri = DocumentFile.isDocumentUri(application, this)
        return if (isDocumentUri) {
            this
        } else {
            DocumentFile.fromTreeUri(application, this)?.findFile(LocalBackupHandler.FileName)?.uri
        }
    }

    private fun OutputStream.writeText(text: String) = bufferedWriter().use { bufferedWriter ->
        bufferedWriter.write(text)
    }

    private fun InputStream.readText(): String = bufferedReader().use { bufferedReader ->
        bufferedReader.readText()
    }

}