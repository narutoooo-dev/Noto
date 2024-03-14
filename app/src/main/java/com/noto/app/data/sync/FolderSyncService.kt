package com.noto.app.data.sync

import com.noto.app.data.model.mapper.FolderMapper
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FolderSyncService(
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val localFolderDataSource: LocalFolderDataSource,
    private val folderMapper: FolderMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) {

    suspend fun runManualFolderSyncService(timestamp: String) = runCatching {
        withContext(coroutineDispatcher) {
            remoteFolderDataSource.getRemoteFoldersSince(timestamp)
                .map { folderMapper.mapRemoteFolderToLocalFolder(it) }
                .forEach { localFolderDataSource.upsertLocalFolder(it) }
        }
    }

    suspend fun startFolderSyncService() {
        coroutineScope {
            withContext(coroutineDispatcher) {
                launch { initCreateFolderListener() }
                launch { initUpdateFolderListener() }
                launch { initDeleteFolderListener() }
                launch { remoteFolderDataSource.subscribeToRemoteFolderListeners() }
            }
        }
    }

    suspend fun stopFolderSyncService() {
        withContext(coroutineDispatcher) {
            remoteFolderDataSource.unsubscribeToRemoteFolderListeners()
        }
    }

    private suspend fun initCreateFolderListener() {
        withContext(coroutineDispatcher) {
            remoteFolderDataSource.createRemoteFolderListener()
                .map(folderMapper::mapRemoteFolderToLocalFolder)
                .filter { localFolderDataSource.getLocalFolderByRemoteId(it.remoteId).first() == null }
                .collect(localFolderDataSource::createLocalFolder)
        }
    }

    private suspend fun initUpdateFolderListener() {
        withContext(coroutineDispatcher) {
            remoteFolderDataSource.updateRemoteFolderListener()
                .map(folderMapper::mapRemoteFolderToLocalFolder)
                .map {
                    val localId = localFolderDataSource.getLocalFolderByRemoteId(it.remoteId).first()?.id ?: 0L
                    it.copy(id = localId)
                }
                .collect(localFolderDataSource::updateLocalFolder)
        }
    }

    private suspend fun initDeleteFolderListener() {
        withContext(coroutineDispatcher) {
            remoteFolderDataSource.deleteRemoteFolderListener()
                .collect(localFolderDataSource::deleteLocalFolderByRemoteId)
        }
    }

}