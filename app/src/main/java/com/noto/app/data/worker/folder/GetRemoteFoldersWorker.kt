package com.noto.app.data.worker.folder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.cache.RemoteItemCacheHandler
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteFoldersWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteFolderWorker {

    private val remoteFolderCacheHandler by inject<RemoteItemCacheHandler<RemoteFolder>>()

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            remoteFolderDataSource.getAllRemoteFolders().also { remoteFolderCacheHandler.cacheRemoteItems(it) }
            Result.success()
        } catch (exception: Throwable) {
            when (exception) {
                NotoException.Entity.InvalidLocalItem -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}