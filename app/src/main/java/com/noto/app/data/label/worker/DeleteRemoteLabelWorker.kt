package com.noto.app.data.label.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.RemoteItemWorker
import com.noto.app.domain.NotoException
import kotlinx.coroutines.withContext

class DeleteRemoteLabelWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteLabelWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteLabelId = inputData.getString(RemoteItemWorker.RemoteLabelId)
            if (remoteLabelId != null) {
                remoteLabelDataSource.deleteRemoteLabelById(remoteLabelId)
                Result.success()
            } else {
                NotoException.Entity.MissingRemoteId()
            }
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}