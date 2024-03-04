package com.noto.app.data.sync

import com.noto.app.data.model.mapper.NoteMapper
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteSyncService(
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val noteMapper: NoteMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) {

    suspend fun startNoteSyncService() {
        coroutineScope {
            withContext(coroutineDispatcher) {
                launch { initCreateNoteListener() }
                launch { initUpdateNoteListener() }
                launch { initDeleteNoteListener() }
                launch { remoteNoteDataSource.subscribeToRemoteNoteListeners() }
            }
        }
    }

    suspend fun stopNoteSyncService() {
        withContext(coroutineDispatcher) {
            remoteNoteDataSource.unsubscribeToRemoteNoteListeners()
        }
    }

    private suspend fun initCreateNoteListener() {
        withContext(coroutineDispatcher) {
            remoteNoteDataSource.createRemoteNoteListener()
                .map(noteMapper::mapRemoteNoteToLocalNote)
                .filter { localNoteDataSource.getLocalNoteByRemoteId(it.remoteId).first() == null }
                .collect(localNoteDataSource::createLocalNote)
        }
    }

    private suspend fun initUpdateNoteListener() {
        withContext(coroutineDispatcher) {
            remoteNoteDataSource.updateRemoteNoteListener()
                .map(noteMapper::mapRemoteNoteToLocalNote)
                .map {
                    val localId = localNoteDataSource.getLocalNoteByRemoteId(it.remoteId).first()?.id ?: 0L
                    it.copy(id = localId)
                }
                .collect(localNoteDataSource::updateLocalNote)
        }
    }

    private suspend fun initDeleteNoteListener() {
        withContext(coroutineDispatcher) {
            remoteNoteDataSource.deleteRemoteNoteListener()
                .collect(localNoteDataSource::deleteLocalNoteByRemoteId)
        }
    }

}