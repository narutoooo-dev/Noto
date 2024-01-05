package com.noto.app.data.worker.note

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.cache.RemoteItemCacheHandler
import com.noto.app.data.model.remote.RemoteNote
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.util.KoinModules
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteNotesWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteNoteWorker {

    private val remoteNoteCacheHandler by inject<RemoteItemCacheHandler<RemoteNote>>(KoinModules.Qualifiers.RemoteNoteCacheHandler)

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteItemWorker.RemoteFolderId)
            val remoteNotes = if (remoteFolderId == null) {
                remoteNoteDataSource.getAllRemoteNotes()
            } else {
                remoteNoteDataSource.getRemoteNotesByFolderId(remoteFolderId)
            }
            remoteNoteCacheHandler.cacheRemoteItems(remoteNotes)
            Result.success()
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}