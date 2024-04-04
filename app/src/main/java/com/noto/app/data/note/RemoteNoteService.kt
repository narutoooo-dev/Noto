package com.noto.app.data.note

interface RemoteNoteService {

    fun getAllRemoteNotes()

    fun getRemoteNotesByFolderId(remoteFolderId: String)

    fun createRemoteNote(remoteNoteId: String)

    fun updateRemoteNote(remoteNoteId: String, oldRemoteNoteLabelIds: List<String>, newRemoteNoteLabelIds: List<String>)

    fun deleteRemoteNote(remoteNoteId: String)

}