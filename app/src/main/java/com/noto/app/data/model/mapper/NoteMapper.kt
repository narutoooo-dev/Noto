package com.noto.app.data.model.mapper

import com.noto.app.crypto.EncryptionHandler
import com.noto.app.data.model.local.LocalLabel
import com.noto.app.data.model.local.LocalNote
import com.noto.app.data.model.remote.RemoteNote
import com.noto.app.domain.model.Note
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class NoteMapper(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val encryptionHandler: EncryptionHandler,
    private val labelMapper: LabelMapper,
    private val propertyMapper: PropertyMapper,
) {

    suspend fun mapDomainNoteToLocalNote(domainNote: Note): LocalNote {
        return with(domainNote) {
            val localNote = localNoteDataSource.getLocalNoteById(id).firstOrNull()
            val remoteId = localNote?.remoteId ?: UUID.randomUUID().toString()
            LocalNote(
                id = id,
                remoteId = remoteId,
                folderId = folderId,
                title = title,
                body = body,
                position = position,
                creationDate = propertyMapper.mapDomainInstantToLocalInstant(creationDate),
                isPinned = isPinned,
                isArchived = isArchived,
                reminderDate = reminderDate?.let(propertyMapper::mapDomainInstantToLocalInstant),
                isVaulted = isVaulted,
                accessDate = propertyMapper.mapDomainInstantToLocalInstant(accessDate),
                scrollingPosition = scrollingPosition,
            )
        }
    }

    suspend fun mapLocalNoteToDomainNote(localNote: LocalNote, localLabels: List<LocalLabel>): Note {
        return with(localNote) {
            val labels = localLabels.map { labelMapper.mapLocalLabelToDomainLabel(it) }
            Note(
                id = id,
                folderId = folderId,
                title = title,
                body = body,
                position = position,
                creationDate = propertyMapper.mapLocalInstantToDomainInstant(creationDate),
                isPinned = isPinned,
                isArchived = isArchived,
                reminderDate = reminderDate?.let(propertyMapper::mapLocalInstantToDomainInstant),
                isVaulted = isVaulted,
                accessDate = propertyMapper.mapLocalInstantToDomainInstant(accessDate),
                scrollingPosition = scrollingPosition,
                labels = labels,
            )
        }
    }

    suspend fun mapLocalNoteToRemoteNote(localNote: LocalNote): RemoteNote {
        return with(localNote) {
            val localFolder = localFolderDataSource.getLocalFolderById(folderId).first()!!
            val keyset = localFolder.keyset!!
            val encryptedContent = encryptionHandler.encryptItem(keyset, this.copy(id = 0L, folderId = 0L))
            RemoteNote(
                id = UUID.fromString(remoteId),
                folderId = UUID.fromString(localFolder.remoteId),
                encryptedContent = encryptedContent,
            )
        }
    }

    suspend fun mapRemoteNoteToLocalNote(remoteNote: RemoteNote): LocalNote {
        return with(remoteNote) {
            val localFolder = localFolderDataSource.getLocalFolderByRemoteId(folderId.toString()).first()!!
            val keyset = localFolder.keyset!!
            val decryptedContent = encryptionHandler.decryptItem<LocalNote>(keyset, encryptedContent)
            decryptedContent.copy(id = 0L, remoteId = id.toString(), folderId = localFolder.id)
        }
    }

}