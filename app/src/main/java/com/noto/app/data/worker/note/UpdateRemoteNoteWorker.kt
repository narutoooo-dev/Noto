package com.noto.app.data.worker.note

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.model.NotoException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class UpdateRemoteNoteWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteNoteWorker {
    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteNoteId = inputData.getString(RemoteItemWorker.RemoteNoteId)
            val oldRemoteNoteLabelIds = inputData.getStringArray(RemoteItemWorker.OldRemoteNoteLabelIds) ?: emptyArray()
            val newRemoteNoteLabelIds = inputData.getStringArray(RemoteItemWorker.NewRemoteNoteLabelIds) ?: emptyArray()
            if (remoteNoteId != null) {
                val localNote = localNoteDataSource.getLocalNoteByRemoteId(remoteNoteId).firstOrNull()
                if (localNote != null) {
                    val remoteNote = noteMapper.mapLocalNoteToRemoteNote(localNote)
                    val newLocalNoteLabels = newRemoteNoteLabelIds
                        .mapNotNull { remoteNoteLabelId -> localNoteLabelDataSource.getNoteLabelByRemoteId(remoteNoteLabelId).first() }
                        .map { noteLabelMapper.mapLocalNoteLabelToRemoteNoteLabel(it) }
                    remoteNoteDataSource.updateRemoteNote(remoteNote)
                    newLocalNoteLabels.forEach { remoteNoteLabelDataSource.createRemoteNoteLabel(it) }
                    oldRemoteNoteLabelIds.forEach { remoteNoteLabelId -> remoteNoteLabelDataSource.deleteRemoteNoteLabelById(remoteNoteLabelId) }
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