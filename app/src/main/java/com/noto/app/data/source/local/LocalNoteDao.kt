package com.noto.app.data.source.local

import androidx.room.*
import com.noto.app.data.model.local.LocalNote
import com.noto.app.domain.source.local.LocalNoteDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalNoteDao : LocalNoteDataSource {

    @Query("SELECT * FROM notes")
    override fun getAllLocalNotes(): Flow<List<LocalNote>>

    @Query("SELECT notes.* FROM notes JOIN folders ON folders.id = notes.folder_id WHERE folders.is_archived = 0 AND folders.is_vaulted = 0 AND notes.is_archived = 0")
    override fun getMainLocalNotes(): Flow<List<LocalNote>>

    @Query("SELECT notes.* FROM notes JOIN folders ON folders.id = notes.folder_id WHERE folders.is_archived = 0 AND folders.is_vaulted = 0 AND notes.is_archived = 1")
    override fun getArchivedLocalNotes(): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE folder_id = :localFolderId AND is_archived = 0")
    override fun getLocalNotesByFolderId(localFolderId: Long): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE folder_id = :localFolderId AND is_archived = 1")
    override fun getArchivedLocalNotesByFolderId(localFolderId: Long): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE id = :localNoteId")
    override fun getLocalNoteById(localNoteId: Long): Flow<LocalNote>

    @Query("SELECT * FROM notes WHERE remote_id = :remoteNoteId")
    override fun getLocalNoteByRemoteId(remoteNoteId: String): Flow<LocalNote?>

    @Query("SELECT COUNT(*) FROM notes WHERE folder_id = :localFolderId AND is_archived = 0")
    override fun countMainLocalNotesByFolderId(localFolderId: Long): Flow<Int>

    @Insert
    override suspend fun createLocalNote(localNote: LocalNote): Long

    @Update
    override suspend fun updateLocalNote(localNote: LocalNote)

    @Delete
    override suspend fun deleteLocalNote(localNote: LocalNote)

    @Query("DELETE FROM notes")
    override suspend fun clearLocalNotes()

}