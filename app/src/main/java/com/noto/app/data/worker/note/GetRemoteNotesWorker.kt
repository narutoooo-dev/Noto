package com.noto.app.data.worker.note

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.worker.RemoteItemWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class GetRemoteNotesWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteNoteWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteNoteWorker.RemoteFolderId)
            val remoteNotes = if (remoteFolderId == null) {
                remoteNoteDataSource.getAllRemoteNotes()
            } else {
                remoteNoteDataSource.getRemoteNotesByFolderId(remoteFolderId)
            }
            remoteNotes.forEach { remoteNote ->
                val databaseLocalNote = localNoteDataSource.getLocalNoteByRemoteId(remoteNote.id.toString()).first()
                val remoteLocalNote = remoteNote.toLocalNote()
                if (databaseLocalNote == null) {
                    localNoteDataSource.createLocalNote(remoteLocalNote.copy(id = RemoteItemWorker.NewItemId))
                } else {
                    localNoteDataSource.updateLocalNote(remoteLocalNote.copy(id = databaseLocalNote.id))
                }
            }
            Result.success()
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}