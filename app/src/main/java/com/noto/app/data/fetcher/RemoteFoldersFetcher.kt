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
            val remoteLocalFolder = folderMapper.mapRemoteFolderToLocalFolder(remoteFolder)
            val isGeneralFolder = remoteLocalFolder.title.isBlank()
            if (isGeneralFolder) {
                val localGeneralFolder = localFolderDataSource.getLocalFolderById(RemoteItemWorker.GeneralFolderId).first()
                val remoteLocalGeneralFolder = remoteLocalFolder.copy(id = RemoteItemWorker.GeneralFolderId)
                if (localGeneralFolder == null) {
                    localFolderDataSource.createLocalFolder(remoteLocalGeneralFolder)
                } else {
                    localFolderDataSource.updateLocalFolder(remoteLocalGeneralFolder)
                }
            } else {
                val databaseLocalFolder = localFolderDataSource.getLocalFolderByRemoteId(remoteFolder.id.toString()).first()
                if (databaseLocalFolder == null) {
                    localFolderDataSource.createLocalFolder(remoteLocalFolder.copy(id = RemoteItemWorker.NewItemId))
                } else {
                    localFolderDataSource.updateLocalFolder(remoteLocalFolder.copy(id = databaseLocalFolder.id))
                }
            }
        }
    }
}