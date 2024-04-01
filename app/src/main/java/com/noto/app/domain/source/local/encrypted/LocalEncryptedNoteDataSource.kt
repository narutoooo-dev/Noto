package com.noto.app.domain.source.local.encrypted

import com.noto.app.data.model.local.encrypted.LocalEncryptedNote
import kotlinx.coroutines.flow.Flow

interface LocalEncryptedNoteDataSource {

    fun getAllLocalEncryptedNotes(): Flow<List<LocalEncryptedNote>>

    fun getMainLocalEncryptedNotesByFolderId(localFolderId: Long): Flow<List<LocalEncryptedNote>>

    fun getArchivedLocalEncryptedNotesByFolderId(localFolderId: Long): Flow<List<LocalEncryptedNote>>

    fun getLocalEncryptedNoteById(localNoteId: Long): Flow<LocalEncryptedNote?>

    fun getLocalEncryptedNoteByRemoteId(remoteNoteId: String): Flow<LocalEncryptedNote?>

    fun countMainLocalEncryptedNotesByFolderId(localFolderId: Long): Flow<Int>

    suspend fun checkIfLocalEncryptedNoteExistsById(localNoteId: Long): Boolean

    suspend fun createLocalEncryptedNote(localEncryptedNote: LocalEncryptedNote): Long

    suspend fun updateLocalEncryptedNote(localEncryptedNote: LocalEncryptedNote)

    suspend fun deleteLocalEncryptedNote(localEncryptedNote: LocalEncryptedNote)

    suspend fun clearLocalEncryptedNotes()

}