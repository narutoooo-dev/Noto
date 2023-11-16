package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteNote

interface RemoteNoteDataSource {

    suspend fun getAllRemoteNotes(): List<RemoteNote>

    suspend fun getRemoteNotesByFolderId(remoteFolderId: String): List<RemoteNote>

    suspend fun createRemoteNote(remoteNote: RemoteNote)

    suspend fun updateRemoteNote(remoteNote: RemoteNote)

    suspend fun deleteRemoteNoteById(remoteNoteId: String)

}