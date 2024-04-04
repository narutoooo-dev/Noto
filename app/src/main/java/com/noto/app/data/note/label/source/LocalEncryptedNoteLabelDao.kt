package com.noto.app.data.note.label.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.noto.app.data.note.label.model.LocalEncryptedNoteLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalEncryptedNoteLabelDao : LocalEncryptedNoteLabelDataSource {

    @Query("SELECT * FROM encrypted_note_labels")
    override fun getAllLocalEncryptedNoteLabels(): Flow<List<LocalEncryptedNoteLabel>>

    @Query("SELECT * FROM encrypted_note_labels WHERE note_id = :localNoteId")
    override fun getLocalEncryptedNoteLabelsByNoteId(localNoteId: Long): Flow<List<LocalEncryptedNoteLabel>>

    @Insert
    override suspend fun createLocalEncryptedNoteLabel(localEncryptedNoteLabel: LocalEncryptedNoteLabel)

    @Delete
    override suspend fun deleteLocalEncryptedNoteLabel(localEncryptedNoteLabel: LocalEncryptedNoteLabel)

    @Query("DELETE FROM encrypted_note_labels")
    override suspend fun clearLocalEncryptedNoteLabels()

}