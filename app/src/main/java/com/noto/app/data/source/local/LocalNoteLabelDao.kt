package com.noto.app.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.noto.app.data.model.local.LocalNoteLabel
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalNoteLabelDao : LocalNoteLabelDataSource {

    @Query("SELECT * FROM note_labels WHERE note_id = :noteId")
    override fun getNoteLabelsByNoteId(noteId: Long): Flow<List<LocalNoteLabel>>

    @Query("SELECT * FROM note_labels")
    override fun getNoteLabels(): Flow<List<LocalNoteLabel>>

    @Insert
    override suspend fun createNoteLabel(noteLabel: LocalNoteLabel)

    @Query("DELETE FROM note_labels WHERE note_id = :noteId AND label_id = :labelId")
    override suspend fun deleteNoteLabel(noteId: Long, labelId: Long)

    @Query("DELETE FROM note_labels")
    override suspend fun clearNoteLabels()

}