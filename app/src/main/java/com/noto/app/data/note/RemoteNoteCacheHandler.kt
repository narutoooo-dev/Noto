package com.noto.app.data.note

import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.note.model.RemoteNote
import com.noto.app.data.note.source.LocalNoteDataSource
import kotlinx.coroutines.flow.first

class RemoteNoteCacheHandler(
    private val localNoteDataSource: LocalNoteDataSource,
    private val noteMapper: NoteMapper,
) : RemoteItemCacheHandler<RemoteNote> {
    override suspend fun cacheRemoteItems(remoteItems: List<RemoteNote>) {
        remoteItems.forEach { remoteNote ->
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