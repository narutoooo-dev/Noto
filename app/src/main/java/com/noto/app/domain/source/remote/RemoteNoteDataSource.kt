package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteNote
import kotlinx.coroutines.flow.Flow

interface RemoteNoteDataSource {

    suspend fun getAllRemoteNotes(): List<RemoteNote>

    suspend fun getRemoteNotesSince(timestamp: String): List<RemoteNote>

    suspend fun getRemoteNotesByFolderId(remoteFolderId: String): List<RemoteNote>

    suspend fun createRemoteNote(remoteNote: RemoteNote)

    suspend fun updateRemoteNote(remoteNote: RemoteNote)

    suspend fun deleteRemoteNoteById(remoteNoteId: String)

    suspend fun subscribeToRemoteNoteListeners()

    suspend fun unsubscribeToRemoteNoteListeners()

    suspend fun createRemoteNoteListener(): Flow<RemoteNote>

    suspend fun updateRemoteNoteListener(): Flow<RemoteNote>

    suspend fun deleteRemoteNoteListener(): Flow<String>

}