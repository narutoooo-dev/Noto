package com.noto.app.data.label.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.label.model.RemoteLabel
import com.noto.app.di.KoinModules
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteLabelsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteLabelWorker {

    private val remoteLabelCacheHandler by inject<RemoteItemCacheHandler<RemoteLabel>>(KoinModules.Qualifiers.RemoteLabelCacheHandler)

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