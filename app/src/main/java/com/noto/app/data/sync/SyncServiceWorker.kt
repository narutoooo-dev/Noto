package com.noto.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncServiceWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val manualSyncServiceManager by inject<ManualSyncServiceManager>()

    override suspend fun doWork(): Result {
        return manualSyncServiceManager.runManualSyncServices()
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.failure() }
            )
    }
}