package com.noto.app.data.worker.folder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class UpdateRemoteFolderWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteFolderWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteItemWorker.RemoteFolderId)
            if (remoteFolderId != null) {
                val localFolder = localFolderDataSource.getLocalFolderByRemoteId(remoteFolderId).firstOrNull()
                if (localFolder != null) {
                    val remoteFolder = folderMapper.mapLocalFolderToRemoteFolder(localFolder)
                    remoteFolderDataSource.updateRemoteFolder(remoteFolder)
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