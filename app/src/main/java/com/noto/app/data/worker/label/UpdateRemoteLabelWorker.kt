package com.noto.app.data.worker.label

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class UpdateRemoteLabelWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteLabelWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteLabelId = inputData.getString(RemoteItemWorker.RemoteLabelId)
            if (remoteLabelId != null) {
                val localLabel = localLabelDataSource.getLocalLabelByRemoteId(remoteLabelId).firstOrNull()
                if (localLabel != null) {
                    val remoteLabel = localLabel.toRemoteLabel()
                    remoteLabelDataSource.updateRemoteLabel(remoteLabel)
                } else {
                    NotoException.Entity.InvalidLocalItem()
                }
                Result.success()
            } else {
                NotoException.Entity.MissingRemoteId()
            }
        } catch (exception: Throwable) {
            when (exception) {
                NotoException.Entity.InvalidLocalItem -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}