package com.noto.app.data.note.label.source

import com.noto.app.data.note.label.model.LocalEncryptedNoteLabel
import kotlinx.coroutines.flow.Flow

interface LocalEncryptedNoteLabelDataSource {

    fun getAllLocalEncryptedNoteLabels(): Flow<List<LocalEncryptedNoteLabel>>

    fun getLocalEncryptedNoteLabelsByNoteId(localNoteId: Long): Flow<List<LocalEncryptedNoteLabel>>

    suspend fun createLocalEncryptedNoteLabel(localEncryptedNoteLabel: LocalEncryptedNoteLabel)

    suspend fun deleteLocalEncryptedNoteLabel(localEncryptedNoteLabel: LocalEncryptedNoteLabel)

    suspend fun clearLocalEncryptedNoteLabels()

}