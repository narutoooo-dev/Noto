package com.noto.app.data.worker.label

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.cache.RemoteItemCacheHandler
import com.noto.app.data.model.remote.RemoteLabel
import com.noto.app.data.worker.RemoteItemWorker
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteLabelsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteLabelWorker {

    private val remoteLabelCacheHandler by inject<RemoteItemCacheHandler<RemoteLabel>>()

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteItemWorker.RemoteFolderId)
            val remoteLabels = if (remoteFolderId == null) {
                remoteLabelDataSource.getAllRemoteLabels()
            } else {
                remoteLabelDataSource.getRemoteLabelsByFolderId(remoteFolderId)
            }
            remoteLabelCacheHandler.cacheRemoteItems(remoteLabels)
            Result.success()
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}