package com.noto.app.data.worker.label

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class GetRemoteLabelsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteLabelWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteItemWorker.RemoteFolderId)
            if (remoteFolderId != null) {
                val remoteLabels = remoteLabelDataSource.getRemoteLabelsByFolderId(remoteFolderId)
                remoteLabels.forEach { remoteLabel ->
                    val databaseLocalLabel = localLabelDataSource.getLocalLabelByRemoteId(remoteLabel.id.toString()).first()
                    val remoteLocalLabel = labelMapper.mapRemoteLabelToLocalLabel(remoteLabel)
                    if (databaseLocalLabel == null) {
                        localLabelDataSource.createLocalLabel(remoteLocalLabel.copy(id = RemoteItemWorker.NewItemId))
                    } else {
                        localLabelDataSource.updateLocalLabel(remoteLocalLabel.copy(id = databaseLocalLabel.id))
                    }
                }
            } else {
                NotoException.Entity.MissingRemoteId()
            }
            Result.success()
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}