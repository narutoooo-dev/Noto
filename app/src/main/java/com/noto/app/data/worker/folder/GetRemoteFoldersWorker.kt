package com.noto.app.data.worker.folder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val GeneralFolderId = -1L
private const val NewFolderId = 0L

class GetRemoteFoldersWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteFolderWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            remoteFolderDataSource.getRemoteFolders().forEach { remoteFolder ->
                val databaseLocalFolder = localFolderDataSource.getLocalFolderByRemoteId(remoteFolder.id.toString()).first()
                val remoteLocalFolder = remoteFolder.toLocalFolder()
                if (databaseLocalFolder == null) {
                    val isGeneralFolder = remoteLocalFolder.title.isBlank()
                    val id = if (isGeneralFolder) GeneralFolderId else NewFolderId
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