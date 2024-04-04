package com.noto.app.data.note

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.noto.app.data.AndroidRemoteItemService
import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.note.worker.CreateRemoteNoteWorker
import com.noto.app.data.note.worker.DeleteRemoteNoteWorker
import com.noto.app.data.note.worker.GetRemoteNotesWorker
import com.noto.app.data.note.worker.UpdateRemoteNoteWorker

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