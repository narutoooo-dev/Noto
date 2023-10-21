package com.noto.app.data.service

import android.content.Context
import androidx.work.*
import com.noto.app.data.worker.folder.*
import com.noto.app.domain.service.RemoteFolderService

private const val GetRemoteFoldersUniqueWorkName = "GetRemoteFolders"

class AndroidRemoteFolderService(context: Context) : RemoteFolderService {

    private val workManager by lazy { WorkManager.getInstance(context) }

    override fun getRemoteFolders() {
        val workRequest = OneTimeWorkRequestBuilder<GetRemoteFoldersWorker>()
            .setConstraints(buildConstraints())
            .build()
        workManager.enqueueUniqueWork(GetRemoteFoldersUniqueWorkName, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun createRemoteFolder(remoteFolderId: String) {
        val workRequest = createOneTimeRequest<CreateRemoteFolderWorker>(remoteFolderId)
        workManager.enqueueUniqueWork(remoteFolderId, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun updateRemoteFolder(remoteFolderId: String) {
        val workRequest = createOneTimeRequest<UpdateRemoteFolderWorker>(remoteFolderId)
        workManager.enqueueUniqueWork(remoteFolderId, ExistingWorkPolicy.APPEND, workRequest)
    }

    override fun deleteRemoteFolder(remoteFolderId: String) {
        val workRequest = createOneTimeRequest<DeleteRemoteFolderWorker>(remoteFolderId)
        workManager.enqueueUniqueWork(remoteFolderId, ExistingWorkPolicy.APPEND, workRequest)
    }

    private inline fun <reified W : ListenableWorker> createOneTimeRequest(remoteFolderId: String): OneTimeWorkRequest {
        val workData = buildWorkData(remoteFolderId)
        val workConstraints = buildConstraints()
        return OneTimeWorkRequestBuilder<W>()
            .setConstraints(workConstraints)
            .setInputData(workData)
            .build()
    }

    private fun buildWorkData(remoteFolderId: String) = workDataOf(RemoteFolderWorker.RemoteFolderId to remoteFolderId)

    private fun buildConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

}