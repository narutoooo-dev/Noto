package com.noto.app.domain.service

interface RemoteNoteService {

    fun getAllRemoteNotes()

    fun getRemoteNotesByFolderId(remoteFolderId: String)

    fun createRemoteNote(remoteNoteId: String)

    fun updateRemoteNote(remoteNoteId: String, oldRemoteNoteLabelIds: List<String>, newRemoteNoteLabelIds: List<String>)

    fun deleteRemoteNote(remoteNoteId: String)

}