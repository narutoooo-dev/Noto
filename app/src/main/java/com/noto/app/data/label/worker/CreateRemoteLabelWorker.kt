package com.noto.app.data.label.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.RemoteItemWorker
import com.noto.app.domain.NotoException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CreateRemoteLabelWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteLabelWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteLabelId = inputData.getString(RemoteItemWorker.RemoteLabelId)
            if (remoteLabelId != null) {
                val localLabel = localLabelDataSource.getLocalLabelByRemoteId(remoteLabelId).first()
                if (localLabel != null) {
                    val remoteLabel = labelMapper.mapLocalLabelToRemoteLabel(localLabel)
                    remoteLabelDataSource.createRemoteLabel(remoteLabel)
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