package com.noto.app.data.sync

import com.noto.app.data.model.mapper.NoteLabelMapper
import com.noto.app.data.model.mapper.NoteMapper
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import com.noto.app.domain.source.remote.RemoteNoteLabelDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteSyncService(
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val remoteNoteLabelDataSource: RemoteNoteLabelDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val noteMapper: NoteMapper,
    private val noteLabelMapper: NoteLabelMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) {

    suspend fun startNoteSyncService() {
        coroutineScope {
            withContext(coroutineDispatcher) {
                launch { initCreateNoteListener() }
                launch { initCreateNoteLabelListener() }
                launch { initUpdateNoteListener() }
                launch { initDeleteNoteListener() }
                launch { initDeleteNoteLabelListener() }
                launch { remoteNoteDataSource.subscribeToRemoteNoteListeners() }
                launch { remoteNoteLabelDataSource.subscribeToRemoteNoteLabelListeners() }
            }
        }
    }

    suspend fun stopNoteSyncService() {
        coroutineScope {
            withContext(coroutineDispatcher) {
                launch { remoteNoteDataSource.unsubscribeToRemoteNoteListeners() }
                launch { remoteNoteLabelDataSource.unsubscribeToRemoteNoteLabelListeners() }
            }
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

    private suspend fun initCreateNoteLabelListener() {
        withContext(coroutineDispatcher) {
            remoteNoteLabelDataSource.createRemoteNoteLabelListener()
                .map(noteLabelMapper::mapRemoteNoteLabelToLocalNoteLabel)
                .filter { localNoteLabelDataSource.getLocalNoteLabelByRemoteId(it.remoteId).first() == null }
                .collect(localNoteLabelDataSource::createLocalNoteLabel)
        }
    }

    private suspend fun initDeleteNoteLabelListener() {
        withContext(coroutineDispatcher) {
            remoteNoteLabelDataSource.deleteRemoteNoteLabelListener()
                .collect(localNoteLabelDataSource::deleteLocalNoteLabelByRemoteId)
        }
    }

}