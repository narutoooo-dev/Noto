package com.noto.app.domain.source.local

import com.noto.app.data.model.local.LocalNote
import kotlinx.coroutines.flow.Flow

interface LocalNoteDataSource {

    fun getAllLocalNotes(): Flow<List<LocalNote>>

    fun getMainLocalNotes(): Flow<List<LocalNote>>

    fun getArchivedLocalNotes(): Flow<List<LocalNote>>

    fun getLocalNotesByFolderId(localFolderId: Long): Flow<List<LocalNote>>

    fun getArchivedLocalNotesByFolderId(localFolderId: Long): Flow<List<LocalNote>>

    fun getLocalNoteById(localNoteId: Long): Flow<LocalNote>

    fun getLocalNoteByRemoteId(remoteNoteId: String): Flow<LocalNote?>

    fun countMainLocalNotesByFolderId(localFolderId: Long): Flow<Int>

    suspend fun createLocalNote(localNote: LocalNote): Long

    suspend fun updateLocalNote(localNote: LocalNote)

    suspend fun upsertLocalNote(localNote: LocalNote)

    suspend fun deleteLocalNote(localNote: LocalNote)

    suspend fun deleteLocalNoteByRemoteId(remoteNoteId: String)

    suspend fun clearLocalNotes()

}