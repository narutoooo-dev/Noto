package com.noto.app.data.worker.note

import com.noto.app.crypto.EncryptionHandler
import com.noto.app.data.model.local.LocalNote
import com.noto.app.data.model.remote.RemoteNote
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import com.noto.app.util.KoinModules
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.get
import java.util.UUID

sealed interface RemoteNoteWorker : RemoteItemWorker {

    val localNoteDataSource get() = get<LocalNoteDataSource>()

    val remoteNoteDataSource get() = get<RemoteNoteDataSource>()

    private val localFolderDataSource get() = get<LocalFolderDataSource>()

    private val encryptionHandler get() = get<EncryptionHandler>()

    private val json get() = get<Json>(KoinModules.Qualifiers.CryptoJson)

    suspend fun LocalNote.toRemoteNote(): RemoteNote {
        val localFolder = localFolderDataSource.getLocalFolderById(folderId).first()!!
        val keyset = localFolder.keyset!!
        val jsonContent = json.encodeToString(this.copy(id = 0L, folderId = 0L))
        val encodedContent = jsonContent.encodeToByteArray()
        val encryptedContent = encryptionHandler.encryptData(keyset, encodedContent)
        return RemoteNote(
            id = UUID.fromString(remoteId),
            folderId = UUID.fromString(localFolder.remoteId),
            encryptedContent = encryptedContent,
        )
    }

    suspend fun RemoteNote.toLocalNote(): LocalNote {
        val localFolder = localFolderDataSource.getLocalFolderByRemoteId(folderId.toString()).first()!!
        val keyset = localFolder.keyset!!
        val decryptedContent = encryptionHandler.decryptData(keyset, encryptedContent)
        val decodedContent = decryptedContent.decodeToString()
        val content = json.decodeFromString<LocalNote>(decodedContent)
        return content.copy(id = 0L, remoteId = id.toString(), folderId = localFolder.id)
    }

}