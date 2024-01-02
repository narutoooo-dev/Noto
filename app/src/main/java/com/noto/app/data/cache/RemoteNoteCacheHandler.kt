package com.noto.app.data.cache

import com.noto.app.data.model.mapper.NoteMapper
import com.noto.app.data.model.remote.RemoteNote
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalNoteDataSource
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