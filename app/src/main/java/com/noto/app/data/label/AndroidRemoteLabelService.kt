package com.noto.app.data.label

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.noto.app.data.AndroidRemoteItemService
import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.label.worker.CreateRemoteLabelWorker
import com.noto.app.data.label.worker.DeleteRemoteLabelWorker
import com.noto.app.data.label.worker.GetRemoteLabelsWorker
import com.noto.app.data.label.worker.UpdateRemoteLabelWorker

class AndroidRemoteLabelService(context: Context) : RemoteLabelService, AndroidRemoteItemService {

    private val workManager by lazy { WorkManager.getInstance(context) }

    override fun getRemoteLabelsByFolderId(remoteFolderId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteFolderId to remoteFolderId)
        val workRequest = OneTimeWorkRequestBuilder<GetRemoteLabelsWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(GetRemoteLabelsWorkName, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun createRemoteLabel(remoteLabelId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteLabelId to remoteLabelId)
        val workRequest = OneTimeWorkRequestBuilder<CreateRemoteLabelWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(remoteLabelId, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun updateRemoteLabel(remoteLabelId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteLabelId to remoteLabelId)
        val workRequest = OneTimeWorkRequestBuilder<UpdateRemoteLabelWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(remoteLabelId, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun deleteRemoteLabel(remoteLabelId: String) {
        val workData = workDataOf(RemoteItemWorker.RemoteLabelId to remoteLabelId)
        val workRequest = OneTimeWorkRequestBuilder<DeleteRemoteLabelWorker>()
            .setConstraints(buildConstraints())
            .setInputData(workData)
            .build()
        workManager.enqueueUniqueWork(remoteLabelId, ExistingWorkPolicy.APPEND, workRequest)
    }

    private companion object {
        private const val GetRemoteLabelsWorkName = "GetRemoteLabels"
    }

}