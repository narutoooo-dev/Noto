package com.noto.app.data.worker.folder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.fetcher.RemoteFoldersFetcher
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteFoldersWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteFolderWorker {

    private val remoteFoldersFetcher by inject<RemoteFoldersFetcher>()

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            remoteFoldersFetcher.fetchRemoteFolders()
            Result.success()
        } catch (exception: Throwable) {
            when (exception) {
                NotoException.Entity.InvalidLocalItem -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}