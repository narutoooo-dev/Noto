package com.noto.app.data.worker.folder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.cache.RemoteItemCacheHandler
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.model.Folder
import com.noto.app.domain.model.NotoException
import com.noto.app.util.KoinModules
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
            } // Else: General folder already exists and fetched, don't create it.
            Result.success()
        } catch (exception: Throwable) {
            when (exception) {
                NotoException.Entity.InvalidLocalItem -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}