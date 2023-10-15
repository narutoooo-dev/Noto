package com.noto.app.data.source.local

import androidx.room.*
import com.noto.app.data.model.local.LocalNote
import com.noto.app.domain.model.FolderIdWithNotesCount
import com.noto.app.domain.source.local.LocalNoteDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalNoteDao : LocalNoteDataSource {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    override fun getAllNotes(): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE is_archived = 0 ORDER BY id DESC")
    override fun getAllMainNotes(): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_archived = 0 ORDER BY id DESC")
    override fun getNotesByFolderId(folderId: Long): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE folder_id = :folderId AND is_archived = 1 ORDER BY id DESC")
    override fun getArchivedNotesByFolderId(folderId: Long): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    override fun getNoteById(noteId: Long): Flow<LocalNote>

    @Query("SELECT folder_id, COUNT(*) as notesCount FROM notes WHERE is_archived = 0 GROUP BY folder_id")
    override fun getFoldersNotesCount(): Flow<List<FolderIdWithNotesCount>>

    @Insert
    override suspend fun createNote(note: LocalNote): Long

    @Update
    override suspend fun updateNote(note: LocalNote)

    @Delete
    override suspend fun deleteNote(note: LocalNote)

    @Query("DELETE FROM notes")
    override suspend fun clearNotes()
}