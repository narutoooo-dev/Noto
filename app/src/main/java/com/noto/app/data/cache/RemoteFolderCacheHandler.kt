package com.noto.app.data.cache

import com.noto.app.data.model.mapper.FolderMapper
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import kotlinx.coroutines.flow.first

class RemoteFolderCacheHandler(
    private val localFolderDataSource: LocalFolderDataSource,
    private val folderMapper: FolderMapper,
) : RemoteItemCacheHandler<RemoteFolder> {
    override suspend fun cacheRemoteItems(remoteItems: List<RemoteFolder>) {
        remoteItems.forEach { remoteFolder ->
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