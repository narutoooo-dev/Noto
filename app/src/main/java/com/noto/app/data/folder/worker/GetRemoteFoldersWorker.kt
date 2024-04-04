package com.noto.app.data.folder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.folder.model.RemoteFolder
import com.noto.app.di.KoinModules
import com.noto.app.domain.NotoException
import com.noto.app.domain.folder.Folder
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteFoldersWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteFolderWorker {

    private val remoteFolderCacheHandler by inject<RemoteItemCacheHandler<RemoteFolder>>(KoinModules.Qualifiers.RemoteFolderCacheHandler)

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolders = remoteFolderDataSource.getAllRemoteFolders()
            remoteFolderCacheHandler.cacheRemoteItems(remoteFolders)
            val localFolders = remoteFolders.map { folderMapper.mapRemoteFolderToLocalFolder(it) }
            val isGeneralFolderCreated = localFolders.any { it.title.isBlank() }
            if (!isGeneralFolderCreated) {
                val folder = Folder.General
                val localFolder = folderMapper.mapDomainFolderToLocalFolder(folder)
                val remoteFolder = folderMapper.mapLocalFolderToRemoteFolder(localFolder)
                localFolderDataSource.createLocalFolder(localFolder)
                remoteFolderDataSource.createRemoteFolder(remoteFolder)
            } // Else: General worker already exists and fetched, don't create it.
            Result.success()
        } catch (exception: Throwable) {
            when (exception) {
                NotoException.Entity.InvalidLocalItem -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}