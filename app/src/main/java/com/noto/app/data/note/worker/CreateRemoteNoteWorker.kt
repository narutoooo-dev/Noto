package com.noto.app.data.note.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.RemoteItemWorker
import com.noto.app.domain.NotoException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CreateRemoteNoteWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteNoteWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteNoteId = inputData.getString(RemoteItemWorker.RemoteNoteId)
            if (remoteNoteId != null) {
                val localNote = localNoteDataSource.getLocalNoteByRemoteId(remoteNoteId).first()
                if (localNote != null) {
                    val remoteNote = noteMapper.mapLocalNoteToRemoteNote(localNote)
                    val localNoteLabels = localNoteLabelDataSource.getLocalNoteLabelsByNoteId(localNote.id).first()
                    val remoteNoteLabels = localNoteLabels.map { noteLabelMapper.mapLocalNoteLabelToRemoteNoteLabel(it) }
                    remoteNoteDataSource.createRemoteNote(remoteNote)
                    remoteNoteLabels.forEach { remoteNoteLabelDataSource.createRemoteNoteLabel(it) }
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