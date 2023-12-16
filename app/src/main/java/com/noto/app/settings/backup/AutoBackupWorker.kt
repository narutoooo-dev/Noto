package com.noto.app.settings.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.KoinModules
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
        localBackupHandler.export(uri, deleteCurrent = true)
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.failure() }
            )
    }

}