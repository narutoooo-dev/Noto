package com.noto.app.data.model.mapper

import com.noto.app.data.model.local.LocalLabel
import com.noto.app.data.model.local.LocalNoteLabel
import com.noto.app.data.model.remote.RemoteNoteLabel
import com.noto.app.domain.model.Label
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import java.util.UUID

class NoteLabelMapper(
    private val localNoteDataSource: LocalNoteDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val labelMapper: LabelMapper,
) {

    suspend fun mapDomainLabelToLocalNoteLabel(domainLabel: Label, localNoteId: Long): LocalNoteLabel {
        return with(domainLabel) {
            val localNoteLabel = localNoteLabelDataSource.getLocalNoteLabelsByNoteId(localNoteId).firstOrNull()?.firstOrNull { it.labelId == id }
            val remoteId = localNoteLabel?.remoteId ?: UUID.randomUUID().toString()
            LocalNoteLabel(remoteId = remoteId, noteId = localNoteId, labelId = id)
        }
    }

    suspend fun mapLocalNoteLabelToDomainLabel(localNoteLabel: LocalNoteLabel, localLabels: List<LocalLabel>): Label {
        val localLabel = localLabels.first { label -> label.id == localNoteLabel.labelId }
        return labelMapper.mapLocalLabelToDomainLabel(localLabel)
    }

    suspend fun mapLocalNoteLabelToRemoteNoteLabel(localNoteLabel: LocalNoteLabel): RemoteNoteLabel {
        return with(localNoteLabel) {
            val localNote = localNoteDataSource.getLocalNoteById(noteId).first()
            val localLabel = localLabelDataSource.getLocalLabelById(labelId).first()
            RemoteNoteLabel(
                id = UUID.fromString(remoteId),
                noteId = UUID.fromString(localNote.remoteId),
                labelId = UUID.fromString(localLabel.remoteId),
                metaData = RemoteNoteLabel.MetaData(createdAt = Clock.System.now().toString()),
            )
        }
    }

    suspend fun mapRemoteNoteLabelToLocalNoteLabel(remoteNoteLabel: RemoteNoteLabel): LocalNoteLabel {
        return with(remoteNoteLabel) {
            val localNote = localNoteDataSource.getLocalNoteByRemoteId(noteId.toString()).first()!!
            val localLabel = localLabelDataSource.getLocalLabelByRemoteId(labelId.toString()).first()!!
            LocalNoteLabel(id = 0L, remoteId = id.toString(), noteId = localNote.id, labelId = localLabel.id)
        }
    }

}