package com.noto.app.data.folder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.RemoteItemWorker
import com.noto.app.domain.NotoException
import kotlinx.coroutines.withContext

class DeleteRemoteFolderWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteFolderWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteItemWorker.RemoteFolderId)
            if (remoteFolderId != null) {
                remoteFolderDataSource.deleteRemoteFolderById(remoteFolderId)
                Result.success()
            } else {
                NotoException.Entity.MissingRemoteId()
            }
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}