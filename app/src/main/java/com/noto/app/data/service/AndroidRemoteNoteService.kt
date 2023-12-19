package com.noto.app.data.service

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.data.worker.note.*
import com.noto.app.domain.service.RemoteNoteService

class AndroidRemoteNoteService(context: Context) : RemoteNoteService, AndroidRemoteItemService {

    private val workManager by lazy { WorkManager.getInstance(context) }

    override fun getAllRemoteNotes() {
        val workData = workDataOf(RemoteItemWorker.RemoteFolderId to null)
        val workRequest = OneTimeWorkRequestBuilder<GetRemoteNotesWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(GetRemoteNotesUniqueWorkName, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun getRemoteNotesByFolderId(remoteFolderId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteFolderId to remoteFolderId)
        val workRequest = OneTimeWorkRequestBuilder<GetRemoteNotesWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(remoteFolderId, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun createRemoteNote(remoteNoteId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteNoteId to remoteNoteId)
        val workRequest = OneTimeWorkRequestBuilder<CreateRemoteNoteWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(remoteNoteId, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun updateRemoteNote(remoteNoteId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteNoteId to remoteNoteId)
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

    companion object {
        const val GetRemoteNotesUniqueWorkName = "GetRemoteNotes"
    }

}