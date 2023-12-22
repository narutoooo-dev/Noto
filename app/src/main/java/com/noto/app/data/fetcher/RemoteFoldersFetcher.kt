package com.noto.app.data.fetcher

import com.noto.app.data.model.mapper.FolderMapper
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import kotlinx.coroutines.flow.first

class RemoteFoldersFetcher(
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val localFolderDataSource: LocalFolderDataSource,
    private val folderMapper: FolderMapper,
) {
    suspend fun fetchRemoteFolders() {
        remoteFolderDataSource.getAllRemoteFolders().forEach { remoteFolder ->
            val databaseLocalFolder = localFolderDataSource.getLocalFolderByRemoteId(remoteFolder.id.toString()).first()
            val remoteLocalFolder = folderMapper.mapRemoteFolderToLocalFolder(remoteFolder)
            if (databaseLocalFolder == null) {
                val isGeneralFolder = remoteLocalFolder.title.isBlank()
                val id = if (isGeneralFolder) RemoteItemWorker.GeneralFolderId else RemoteItemWorker.NewItemId
                localFolderDataSource.createLocalFolder(remoteLocalFolder.copy(id = id))
            } else {
                localFolderDataSource.updateLocalFolder(remoteLocalFolder.copy(id = databaseLocalFolder.id))
            }
        }
    }
}