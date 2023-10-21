package com.noto.app.data.worker.folder

import com.noto.app.data.database.InstantConverter
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import com.noto.app.util.CoroutineDispatcherQualifier
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.UUID

sealed interface RemoteFolderWorker : KoinComponent {

    val coroutineDispatcher get() = get<CoroutineDispatcher>(CoroutineDispatcherQualifier)

    val localFolderDataSource get() = get<LocalFolderDataSource>()

    val remoteFolderDataSource get() = get<RemoteFolderDataSource>()

    companion object {
        const val RemoteFolderId = "remote_folder_id"
    }

    fun RemoteFolder.toLocalFolder(id: Long): LocalFolder {
        return LocalFolder(
            id = id,
            remoteId = this.id.toString(),
            parentId = null,
            title = title,
            position = 0,
            color = 0,
            creationDate = InstantConverter.toString(createdAt)!!,
            layout = 0,
            notePreviewSize = 0,
            isArchived = false,
            isPinned = false,
            isShowNoteCreationDate = false,
            newNoteCursorPosition = 0,
            sortingType = 0,
            sortingOrder = 0,
            grouping = 0,
            groupingOrder = 0,
            isVaulted = false,
            scrollingPosition = 0,
            filteringType = 0,
            openNotesIn = 0,
        )
    }

    suspend fun LocalFolder.toRemoteFolder(): RemoteFolder {
        return RemoteFolder(
            id = UUID.fromString(remoteId),
            title = title,
            createdAt = InstantConverter.toDate(creationDate)!!,
        )
    }

    val RemoteFolder.isGeneralFolder get() = title.isBlank()

}