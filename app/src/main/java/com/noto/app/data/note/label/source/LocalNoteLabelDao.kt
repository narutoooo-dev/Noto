package com.noto.app.data.note.label.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.noto.app.data.note.label.model.LocalNoteLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalNoteLabelDao : LocalNoteLabelDataSource {

    @Query("SELECT * FROM note_labels")
    override fun getAllLocalNoteLabels(): Flow<List<LocalNoteLabel>>

    @Query("SELECT * FROM note_labels WHERE note_id = :localNoteId")
    override fun getLocalNoteLabelsByNoteId(localNoteId: Long): Flow<List<LocalNoteLabel>>

    @Query("SELECT * FROM note_labels WHERE remote_id = :remoteNoteLabelId")
    override fun getLocalNoteLabelByRemoteId(remoteNoteLabelId: String): Flow<LocalNoteLabel?>

    @Insert
    override suspend fun createLocalNoteLabel(localNoteLabel: LocalNoteLabel)

    @Delete
    override suspend fun deleteLocalNoteLabel(localNoteLabel: LocalNoteLabel)

    @Query("DELETE FROM note_labels WHERE remote_id = :remoteNoteLabelId")
    override suspend fun deleteLocalNoteLabelByRemoteId(remoteNoteLabelId: String)

    @Query("DELETE FROM note_labels")
    override suspend fun clearLocalNoteLabels()

}