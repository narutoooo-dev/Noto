package com.noto.app.data.fetcher

import com.noto.app.data.model.mapper.NoteMapper
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import kotlinx.coroutines.flow.first

class RemoteNotesFetcher(
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val noteMapper: NoteMapper,
) {
    suspend fun fetchRemoteNotes(remoteFolderId: String?) {
        val remoteNotes = if (remoteFolderId == null) {
            remoteNoteDataSource.getAllRemoteNotes()
        } else {
            remoteNoteDataSource.getRemoteNotesByFolderId(remoteFolderId)
        }
        remoteNotes.forEach { remoteNote ->
            val databaseLocalNote = localNoteDataSource.getLocalNoteByRemoteId(remoteNote.id.toString()).first()
            val remoteLocalNote = noteMapper.mapRemoteNoteToLocalNote(remoteNote)
            if (databaseLocalNote == null) {
                localNoteDataSource.createLocalNote(remoteLocalNote.copy(id = RemoteItemWorker.NewItemId))
            } else {
                localNoteDataSource.updateLocalNote(remoteLocalNote.copy(id = databaseLocalNote.id))
            }
        }
    }
}