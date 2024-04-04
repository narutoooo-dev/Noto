package com.noto.app.data.folder

import android.content.Context
import androidx.work.*
import com.noto.app.data.AndroidRemoteItemService
import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.folder.worker.CreateRemoteFolderWorker
import com.noto.app.data.folder.worker.DeleteRemoteFolderWorker
import com.noto.app.data.folder.worker.GetRemoteFoldersWorker
import com.noto.app.data.folder.worker.UpdateRemoteFolderWorker

class AndroidRemoteFolderService(context: Context) : RemoteFolderService, AndroidRemoteItemService {

    private val workManager by lazy { WorkManager.getInstance(context) }

    override fun getRemoteFolders() {
        val workRequest = OneTimeWorkRequestBuilder<GetRemoteFoldersWorker>()
            .setConstraints(buildConstraints())
            .build()
        workManager.enqueueUniqueWork(GetRemoteFoldersWorkName, ExistingWorkPolicy.APPEND, workRequest)
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

    private fun buildWorkData(remoteFolderId: String) = workDataOf(RemoteItemWorker.RemoteFolderId to remoteFolderId)

    private companion object {
        private const val GetRemoteFoldersWorkName = "GetRemoteFolders"
    }

}