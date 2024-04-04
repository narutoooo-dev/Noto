package com.noto.app.data.note.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.RemoteItemWorker
import com.noto.app.domain.NotoException
import kotlinx.coroutines.withContext

class DeleteRemoteNoteWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteNoteWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteNoteId = inputData.getString(RemoteItemWorker.RemoteNoteId)
            if (remoteNoteId != null) {
                remoteNoteDataSource.deleteRemoteNoteById(remoteNoteId)
                Result.success()
            } else {
                NotoException.Entity.MissingRemoteId()
            }
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}