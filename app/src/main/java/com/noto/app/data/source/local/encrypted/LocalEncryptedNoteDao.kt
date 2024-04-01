package com.noto.app.data.source.local.encrypted

import androidx.room.*
import com.noto.app.data.model.local.encrypted.LocalEncryptedNote
import com.noto.app.domain.source.local.encrypted.LocalEncryptedNoteDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalEncryptedNoteDao : LocalEncryptedNoteDataSource {

    @Query("SELECT * FROM encrypted_notes")
    override fun getAllLocalEncryptedNotes(): Flow<List<LocalEncryptedNote>>

    @Query("SELECT * FROM encrypted_notes WHERE folder_id = :localFolderId AND is_archived = 0")
    override fun getMainLocalEncryptedNotesByFolderId(localFolderId: Long): Flow<List<LocalEncryptedNote>>

    @Query("SELECT * FROM encrypted_notes WHERE folder_id = :localFolderId AND is_archived = 1")
    override fun getArchivedLocalEncryptedNotesByFolderId(localFolderId: Long): Flow<List<LocalEncryptedNote>>

    @Query("SELECT * FROM encrypted_notes WHERE id = :localNoteId")
    override fun getLocalEncryptedNoteById(localNoteId: Long): Flow<LocalEncryptedNote?>

    @Query("SELECT * FROM encrypted_notes WHERE remote_id = :remoteNoteId")
    override fun getLocalEncryptedNoteByRemoteId(remoteNoteId: String): Flow<LocalEncryptedNote?>

    @Query("SELECT COUNT(*) FROM encrypted_notes WHERE folder_id = :localFolderId AND is_archived = 0")
    override fun countMainLocalEncryptedNotesByFolderId(localFolderId: Long): Flow<Int>

    @Query("SELECT COUNT(1) FROM encrypted_notes WHERE id = :localNoteId")
    override suspend fun checkIfLocalEncryptedNoteExistsById(localNoteId: Long): Boolean

    @Insert
    override suspend fun createLocalEncryptedNote(localEncryptedNote: LocalEncryptedNote): Long

    @Update
    override suspend fun updateLocalEncryptedNote(localEncryptedNote: LocalEncryptedNote)

    @Delete
    override suspend fun deleteLocalEncryptedNote(localEncryptedNote: LocalEncryptedNote)

    @Query("DELETE FROM encrypted_notes")
    override suspend fun clearLocalEncryptedNotes()

}