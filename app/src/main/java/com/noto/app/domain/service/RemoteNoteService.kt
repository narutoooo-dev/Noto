package com.noto.app.domain.service

interface RemoteNoteService {

    fun getAllRemoteNotes()

    fun getRemoteNoteByRemoteFolderId(remoteFolderId: String)

    fun createRemoteNote(remoteNoteId: String)

    fun updateRemoteNote(remoteNoteId: String)

    fun deleteRemoteNote(remoteNoteId: String)

}