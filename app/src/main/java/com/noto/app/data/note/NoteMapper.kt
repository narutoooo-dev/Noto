package com.noto.app.data.note

import com.noto.app.crypto.VaultEncryptionHandler
import com.noto.app.crypto.tink.TinkEncryptionHandler
import com.noto.app.data.PropertyMapper
import com.noto.app.data.folder.FolderMapper
import com.noto.app.data.folder.source.LocalEncryptedFolderDataSource
import com.noto.app.data.folder.source.LocalFolderDataSource
import com.noto.app.data.note.model.LocalEncryptedNote
import com.noto.app.data.note.model.LocalNote
import com.noto.app.data.note.model.RemoteNote
import com.noto.app.data.note.source.LocalEncryptedNoteDataSource
import com.noto.app.data.note.source.LocalNoteDataSource
import com.noto.app.domain.label.Label
import com.noto.app.domain.note.Note
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import java.util.UUID

class NoteMapper(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val localEncryptedFolderDataSource: LocalEncryptedFolderDataSource,
    private val localEncryptedNoteDataSource: LocalEncryptedNoteDataSource,
    private val tinkEncryptionHandler: TinkEncryptionHandler,
    private val folderMapper: FolderMapper,
    private val propertyMapper: PropertyMapper,
    private val vaultEncryptionHandler: VaultEncryptionHandler,
) {

    suspend fun mapDomainNoteToLocalNote(domainNote: Note): LocalNote {
        return with(domainNote) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(folderId)
            val remoteId = getLocalNoteById(id, isLocalFolderVaulted)?.remoteId ?: UUID.randomUUID().toString()
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

    suspend fun mapLocalNoteToDomainNote(localNote: LocalNote, labels: List<Label>): Note {
        return with(localNote) {
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

    suspend fun mapLocalNoteToLocalEncryptedNote(localNote: LocalNote): LocalEncryptedNote {
        return with(localNote) {
            LocalEncryptedNote(
                id = id,
                remoteId = remoteId,
                folderId = folderId,
                isArchived = isArchived,
                content = vaultEncryptionHandler.encryptItem(this),
            )
        }
    }

    suspend fun mapLocalEncryptedNoteToLocalNote(localEncryptedNote: LocalEncryptedNote): LocalNote {
        return with(localEncryptedNote) {
            vaultEncryptionHandler.decryptItem<LocalNote>(content).copy(remoteId = remoteId)
        }
    }

    suspend fun mapLocalNoteToRemoteNote(localNote: LocalNote): RemoteNote {
        return with(localNote) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(folderId)
            val localFolder = folderMapper.getLocalFolderById(folderId, isLocalFolderVaulted)!!
            val keyset = localFolder.keyset!!
            val encryptedContent = tinkEncryptionHandler.encryptItem(keyset, this.copy(id = 0L, folderId = 0L))
            RemoteNote(
                id = UUID.fromString(remoteId),
                folderId = UUID.fromString(localFolder.remoteId),
                encryptedContent = encryptedContent,
                metaData = RemoteNote.MetaData(updatedAt = Clock.System.now().toString()),
            )
        }
    }

    suspend fun mapRemoteNoteToLocalNote(remoteNote: RemoteNote): LocalNote {
        return with(remoteNote) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsByRemoteId(folderId.toString())
            val localFolder = folderMapper.getLocalFolderByRemoteId(folderId.toString(), isLocalFolderVaulted)!!
            val keyset = localFolder.keyset!!
            val decryptedContent = tinkEncryptionHandler.decryptItem<LocalNote>(keyset, encryptedContent)
            decryptedContent.copy(id = 0L, remoteId = id.toString(), folderId = localFolder.id)
        }
    }

    private suspend fun getLocalNoteById(noteId: Long?, isLocalFolderVaulted: Boolean): LocalNote? {
        return noteId?.let { id ->
            if (isLocalFolderVaulted) {
                localEncryptedNoteDataSource.getLocalEncryptedNoteById(id).firstOrNull()
                    ?.let { mapLocalEncryptedNoteToLocalNote(it) }
            } else {
                localNoteDataSource.getLocalNoteById(id).firstOrNull()
            }
        }
    }

}