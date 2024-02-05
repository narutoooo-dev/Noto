package com.noto.app.data.model.mapper

import com.noto.app.data.model.local.LocalNoteLabel
import com.noto.app.data.model.remote.RemoteNoteLabel
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import kotlinx.coroutines.flow.first
import java.util.UUID

class NoteLabelMapper(
    private val localNoteDataSource: LocalNoteDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
) {

    suspend fun mapLocalNoteLabelToRemoteNoteLabel(localNoteLabel: LocalNoteLabel): RemoteNoteLabel {
        return with(localNoteLabel) {
            val localNote = localNoteDataSource.getLocalNoteById(noteId).first()
            val localLabel = localLabelDataSource.getLocalLabelById(labelId).first()
            RemoteNoteLabel(
                id = UUID.fromString(remoteId),
                noteId = UUID.fromString(localNote.remoteId),
                labelId = UUID.fromString(localLabel.remoteId),
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