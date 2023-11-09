package com.noto.app.data.worker.folder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.model.local.LocalGeneralFolderManager
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class CreateRemoteGeneralFolderWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams),
    RemoteFolderWorker {

    private val localGeneralFolderManager by inject<LocalGeneralFolderManager>()

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val isGeneralFolderExists = remoteFolderDataSource.getRemoteFolders().map { it.toLocalFolder() }.any { it.title.isBlank() }
            if (!isGeneralFolderExists) {
                val generalFolder = localGeneralFolderManager.newLocalGeneralFolder()
                val remoteFolder = generalFolder.toRemoteFolder()
                localFolderDataSource.createLocalFolder(generalFolder)
                remoteFolderDataSource.createRemoteFolder(remoteFolder)
            } // Else: General folder already exists, don't create it, but fetch it instead.
            Result.success()
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}