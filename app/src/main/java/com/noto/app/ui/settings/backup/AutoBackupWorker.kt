package com.noto.app.ui.settings.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.di.KoinModules
import com.noto.app.domain.BackupFormat
import com.noto.app.domain.settings.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AutoBackupWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val coroutineDispatcher by inject<CoroutineDispatcher>(KoinModules.Qualifiers.CoroutineDispatcher)

    private val localBackupHandler by inject<LocalBackupHandler>()

    private val settingsRepository by inject<SettingsRepository>()

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        val uri = settingsRepository.autoBackupLocation.first()
        val result = when (settingsRepository.autoBackupFormat.first()) {
            BackupFormat.PlainText -> settingsRepository.exportNotoData()
            BackupFormat.Encrypted -> settingsRepository.exportEncryptedNotoData()
        }
        result.fold(
            onSuccess = { exportedData ->
                localBackupHandler.export(uri, exportedData, deleteCurrent = true)
                    .fold(
                        onSuccess = { Result.success() },
                        onFailure = { Result.failure() }
                    )
            },
            onFailure = { Result.failure() },
        )
    }

}