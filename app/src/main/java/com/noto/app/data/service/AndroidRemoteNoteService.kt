package com.noto.app.data.service

import android.content.Context
import androidx.work.*
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.data.worker.note.CreateRemoteNoteWorker
import com.noto.app.data.worker.note.DeleteRemoteNoteWorker
import com.noto.app.data.worker.note.GetRemoteNotesWorker
import com.noto.app.data.worker.note.UpdateRemoteNoteWorker
import com.noto.app.domain.service.RemoteNoteService

class AndroidRemoteNoteService(context: Context) : RemoteNoteService, AndroidRemoteItemService {

    private val workManager by lazy { WorkManager.getInstance(context) }

    override fun getAllRemoteNotes() {
        val workData = workDataOf(RemoteItemWorker.RemoteFolderId to null)
        val workRequest = OneTimeWorkRequestBuilder<GetRemoteNotesWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(GetAllRemoteNotesWorkName, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun getRemoteNotesByFolderId(remoteFolderId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteFolderId to remoteFolderId)
        val workRequest = OneTimeWorkRequestBuilder<GetRemoteNotesWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(GetRemoteNotesWorkName, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun createRemoteNote(remoteNoteId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteNoteId to remoteNoteId)
        val workRequest = OneTimeWorkRequestBuilder<CreateRemoteNoteWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(remoteNoteId, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun updateRemoteNote(remoteNoteId: String, oldRemoteNoteLabelIds: List<String>, newRemoteNoteLabelIds: List<String>) {
        val workData = workDataOf(
            RemoteItemWorker.RemoteNoteId to remoteNoteId,
            RemoteItemWorker.OldRemoteNoteLabelIds to oldRemoteNoteLabelIds.toTypedArray(),
            RemoteItemWorker.NewRemoteNoteLabelIds to newRemoteNoteLabelIds.toTypedArray(),
        )
        val workRequest = OneTimeWorkRequestBuilder<UpdateRemoteNoteWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(remoteNoteId, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun deleteRemoteNote(remoteNoteId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteNoteId to remoteNoteId)
        val workRequest = OneTimeWorkRequestBuilder<DeleteRemoteNoteWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(remoteNoteId, ExistingWorkPolicy.APPEND, workRequest)
    }

    private companion object {
        private const val GetAllRemoteNotesWorkName = "GetAllRemoteNotes"
        private const val GetRemoteNotesWorkName = "GetRemoteNotes"
    }

}