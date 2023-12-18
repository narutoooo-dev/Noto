package com.noto.app.data.worker.note

import com.noto.app.crypto.EncryptionHandler
import com.noto.app.data.model.local.LocalNote
import com.noto.app.data.model.remote.RemoteNote
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import kotlinx.coroutines.flow.first
import org.koin.core.component.get
import java.util.UUID

sealed interface RemoteNoteWorker : RemoteItemWorker {

    val localNoteDataSource get() = get<LocalNoteDataSource>()

    val remoteNoteDataSource get() = get<RemoteNoteDataSource>()

    private val localFolderDataSource get() = get<LocalFolderDataSource>()

    private val encryptionHandler get() = get<EncryptionHandler>()

    suspend fun LocalNote.toRemoteNote(): RemoteNote {
        val localFolder = localFolderDataSource.getLocalFolderById(folderId).first()!!
        val keyset = localFolder.keyset!!
        val encryptedContent = encryptionHandler.encryptItem(keyset, this.copy(id = 0L, folderId = 0L))
        return RemoteNote(
            id = UUID.fromString(remoteId),
            folderId = UUID.fromString(localFolder.remoteId),
            encryptedContent = encryptedContent,
        )
    }

    suspend fun RemoteNote.toLocalNote(): LocalNote {
        val localFolder = localFolderDataSource.getLocalFolderByRemoteId(folderId.toString()).first()!!
        val keyset = localFolder.keyset!!
        val decryptedContent = encryptionHandler.decryptItem<LocalNote>(keyset, encryptedContent)
        return decryptedContent.copy(id = 0L, remoteId = id.toString(), folderId = localFolder.id)
    }

}