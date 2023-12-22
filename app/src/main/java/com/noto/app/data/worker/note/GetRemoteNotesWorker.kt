package com.noto.app.data.worker.note

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noto.app.data.fetcher.RemoteNotesFetcher
import com.noto.app.data.worker.RemoteItemWorker
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class GetRemoteNotesWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), RemoteNoteWorker {

    private val remoteNotesFetcher by inject<RemoteNotesFetcher>()

    override suspend fun doWork(): Result = withContext(coroutineDispatcher) {
        try {
            val remoteFolderId = inputData.getString(RemoteItemWorker.RemoteFolderId)
            remoteNotesFetcher.fetchRemoteNotes(remoteFolderId)
            Result.success()
        } catch (exception: Throwable) {
            Result.failure()
        }
    }
}