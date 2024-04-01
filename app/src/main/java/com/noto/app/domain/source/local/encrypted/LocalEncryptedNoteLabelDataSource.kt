package com.noto.app.domain.source.local.encrypted

import com.noto.app.data.model.local.encrypted.LocalEncryptedNoteLabel
import kotlinx.coroutines.flow.Flow

interface LocalEncryptedNoteLabelDataSource {

    fun getAllLocalEncryptedNoteLabels(): Flow<List<LocalEncryptedNoteLabel>>

    fun getLocalEncryptedNoteLabelsByNoteId(localNoteId: Long): Flow<List<LocalEncryptedNoteLabel>>

    suspend fun createLocalEncryptedNoteLabel(localEncryptedNoteLabel: LocalEncryptedNoteLabel)

    suspend fun deleteLocalEncryptedNoteLabel(localEncryptedNoteLabel: LocalEncryptedNoteLabel)

    suspend fun clearLocalEncryptedNoteLabels()

}