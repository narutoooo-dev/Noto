package com.noto.app.data.note.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.note.label.model.RemoteNoteLabel
import com.noto.app.data.note.model.RemoteNote
import com.noto.app.di.KoinModules
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteNotesWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteNoteWorker {

    private val remoteNoteCacheHandler by inject<RemoteItemCacheHandler<RemoteNote>>(KoinModules.Qualifiers.RemoteNoteCacheHandler)

    private val remoteNoteLabelCacheHandler by inject<RemoteItemCacheHandler<RemoteNoteLabel>>(KoinModules.Qualifiers.RemoteNoteLabelCacheHandler)

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteItemWorker.RemoteFolderId)
            val remoteNotes = if (remoteFolderId == null) {
                remoteNoteDataSource.getAllRemoteNotes()
            } else {
                remoteNoteDataSource.getRemoteNotesByFolderId(remoteFolderId)
            }
            remoteNoteCacheHandler.cacheRemoteItems(remoteNotes)
            val remoteNoteLabels = remoteNoteLabelDataSource.getAllRemoteNoteLabels()
            remoteNoteLabelCacheHandler.cacheRemoteItems(remoteNoteLabels)
            Result.success()
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}