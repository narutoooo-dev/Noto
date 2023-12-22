package com.noto.app.data.worker.label

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.fetcher.RemoteLabelsFetcher
import com.noto.app.data.worker.RemoteItemWorker
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteLabelsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteLabelWorker {

    private val remoteLabelsFetcher by inject<RemoteLabelsFetcher>()

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteItemWorker.RemoteFolderId)
            remoteLabelsFetcher.fetchRemoteLabels(remoteFolderId)
            Result.success()
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}