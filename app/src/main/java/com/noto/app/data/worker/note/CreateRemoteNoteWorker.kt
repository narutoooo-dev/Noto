package com.noto.app.data.worker.note

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CreateRemoteNoteWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteNoteWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteNoteId = inputData.getString(RemoteNoteWorker.RemoteNoteId)
            if (remoteNoteId != null) {
                val localNote = localNoteDataSource.getLocalNoteByRemoteId(remoteNoteId).first()
                if (localNote != null) {
                    val remoteNote = localNote.toRemoteNote()
                    remoteNoteDataSource.createRemoteNote(remoteNote)
                } else {
                    NotoException.Entity.InvalidLocalItem()
                }
                Result.success()
            } else {
                NotoException.Entity.MissingRemoteId()
            }
        } catch (exception: Throwable) {
            when (exception) {
                NotoException.Entity.InvalidLocalItem -> Result.retry()
                else -> Result.failure()
            }
        }
    }
}