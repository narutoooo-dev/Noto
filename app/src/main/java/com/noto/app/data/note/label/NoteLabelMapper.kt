package com.noto.app.data.note.label

import com.noto.app.data.folder.source.LocalEncryptedFolderDataSource
import com.noto.app.data.label.LabelMapper
import com.noto.app.data.label.model.LocalLabel
import com.noto.app.data.label.source.LocalEncryptedLabelDataSource
import com.noto.app.data.label.source.LocalLabelDataSource
import com.noto.app.data.note.label.model.LocalEncryptedNoteLabel
import com.noto.app.data.note.label.model.LocalNoteLabel
import com.noto.app.data.note.label.model.RemoteNoteLabel
import com.noto.app.data.note.label.source.LocalEncryptedNoteLabelDataSource
import com.noto.app.data.note.label.source.LocalNoteLabelDataSource
import com.noto.app.data.note.source.LocalEncryptedNoteDataSource
import com.noto.app.data.note.source.LocalNoteDataSource
import com.noto.app.domain.label.Label
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import java.util.UUID

class NoteLabelMapper(
    private val localNoteDataSource: LocalNoteDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val localEncryptedFolderDataSource: LocalEncryptedFolderDataSource,
    private val localEncryptedNoteDataSource: LocalEncryptedNoteDataSource,
    private val localEncryptedLabelDataSource: LocalEncryptedLabelDataSource,
    private val localEncryptedNoteLabelDataSource: LocalEncryptedNoteLabelDataSource,
    private val labelMapper: LabelMapper,
) {

    suspend fun mapDomainLabelToLocalNoteLabel(domainLabel: Label, localNoteId: Long): LocalNoteLabel {
        return with(domainLabel) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(folderId)
            val remoteId = getLocalNoteLabelsByNoteId(localNoteId, isLocalFolderVaulted).firstOrNull { it.labelId == id }
                ?.remoteId ?: UUID.randomUUID().toString()
            LocalNoteLabel(
                remoteId = remoteId,
                noteId = localNoteId,
                labelId = id,
            )
        }
    }

    // Return nullable value due to concurrency condition that throws NoSuchElementException making the app crash.
    suspend fun mapLocalNoteLabelToDomainLabel(localNoteLabel: LocalNoteLabel, localLabels: List<LocalLabel>): Label? {
        val localLabel = localLabels.firstOrNull { label -> label.id == localNoteLabel.labelId }
        return localLabel?.let { labelMapper.mapLocalLabelToDomainLabel(it) }
    }

    suspend fun mapLocalNoteLabelToLocalEncryptedNoteLabel(localNoteLabel: LocalNoteLabel): LocalEncryptedNoteLabel {
        return with(localNoteLabel) {
            LocalEncryptedNoteLabel(
                id = id,
                remoteId = remoteId,
                noteId = noteId,
                labelId = labelId,
            )
        }
    }

    suspend fun mapLocalEncryptedNoteLabelToLocalNoteLabel(localEncryptedNoteLabel: LocalEncryptedNoteLabel): LocalNoteLabel {
        return with(localEncryptedNoteLabel) {
            LocalNoteLabel(
                id = id,
                remoteId = remoteId,
                noteId = noteId,
                labelId = labelId,
            )
        }
    }

    suspend fun mapLocalNoteLabelToRemoteNoteLabel(localNoteLabel: LocalNoteLabel): RemoteNoteLabel {
        return with(localNoteLabel) {
            val localNote = localNoteDataSource.getLocalNoteById(noteId).first()
            val localLabel = localLabelDataSource.getLocalLabelById(labelId).first()
            val localEncryptedNote = localEncryptedNoteDataSource.getLocalEncryptedNoteById(noteId).first()
            val localEncryptedLabel = localEncryptedLabelDataSource.getLocalEncryptedLabelById(labelId).first()
            val remoteNoteId = localNote?.remoteId ?: localEncryptedNote?.remoteId!!
            val remoteLabelId = localLabel?.remoteId ?: localEncryptedLabel?.remoteId!!
            RemoteNoteLabel(
                id = UUID.fromString(remoteId),
                noteId = UUID.fromString(remoteNoteId),
                labelId = UUID.fromString(remoteLabelId),
                metaData = RemoteNoteLabel.MetaData(createdAt = Clock.System.now().toString()),
            )
        }
    }

    suspend fun mapRemoteNoteLabelToLocalNoteLabel(remoteNoteLabel: RemoteNoteLabel): LocalNoteLabel {
        return with(remoteNoteLabel) {
            val localNote = localNoteDataSource.getLocalNoteByRemoteId(noteId.toString()).first()
            val localLabel = localLabelDataSource.getLocalLabelByRemoteId(labelId.toString()).first()
            val localEncryptedNote = localEncryptedNoteDataSource.getLocalEncryptedNoteByRemoteId(noteId.toString()).first()
            val localEncryptedLabel = localEncryptedLabelDataSource.getLocalEncryptedLabelByRemoteId(labelId.toString()).first()
            val localNoteId = localNote?.id ?: localEncryptedNote?.id!!
            val localLabelId = localLabel?.id ?: localEncryptedLabel?.id!!
            LocalNoteLabel(id = 0L, remoteId = id.toString(), noteId = localNoteId, labelId = localLabelId)
        }
    }

    private suspend fun getLocalNoteLabelsByNoteId(noteId: Long?, isLocalFolderVaulted: Boolean): List<LocalNoteLabel> {
        return noteId?.let { id ->
            if (isLocalFolderVaulted) {
                localEncryptedNoteLabelDataSource.getLocalEncryptedNoteLabelsByNoteId(id).firstOrNull()
                    ?.map { mapLocalEncryptedNoteLabelToLocalNoteLabel(it) }
            } else {
                localNoteLabelDataSource.getLocalNoteLabelsByNoteId(id).firstOrNull()
            }
        } ?: emptyList()
    }

}