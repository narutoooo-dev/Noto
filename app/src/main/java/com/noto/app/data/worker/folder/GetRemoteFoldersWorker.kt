package com.noto.app.data.worker.folder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class GetRemoteFoldersWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteFolderWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            remoteFolderDataSource.getRemoteFolders().forEach { remoteFolder ->
                val databaseLocalFolder = localFolderDataSource.getLocalFolderByRemoteId(remoteFolder.id.toString()).first()
                val remoteLocalFolder = folderMapper.mapRemoteFolderToLocalFolder(remoteFolder)
                if (databaseLocalFolder == null) {
                    val isGeneralFolder = remoteLocalFolder.title.isBlank()
                    val id = if (isGeneralFolder) RemoteItemWorker.GeneralFolderId else RemoteItemWorker.NewItemId
                    localFolderDataSource.createLocalFolder(remoteLocalFolder.copy(id = id))
                } else {
                    localFolderDataSource.updateLocalFolder(remoteLocalFolder.copy(id = databaseLocalFolder.id))
                }
            }
            Result.success()
        } catch (exception: Throwable) {
            when (exception) {
                NotoException.Entity.InvalidLocalItem -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}