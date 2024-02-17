package com.noto.app.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.noto.app.data.model.local.LocalNoteLabel
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalNoteLabelDao : LocalNoteLabelDataSource {

    @Query("SELECT * FROM note_labels")
    override fun getAllNoteLabels(): Flow<List<LocalNoteLabel>>

    @Query("SELECT * FROM note_labels WHERE note_id = :noteId")
    override fun getNoteLabelsByNoteId(noteId: Long): Flow<List<LocalNoteLabel>>

    @Query("SELECT * FROM note_labels WHERE remote_id = :remoteNoteLabelId")
    override fun getNoteLabelByRemoteId(remoteNoteLabelId: String): Flow<LocalNoteLabel?>

    @Insert
    override suspend fun createNoteLabel(noteLabel: LocalNoteLabel)

    @Delete
    override suspend fun deleteNoteLabel(noteLabel: LocalNoteLabel)

    @Query("DELETE FROM note_labels")
    override suspend fun clearNoteLabels()

}