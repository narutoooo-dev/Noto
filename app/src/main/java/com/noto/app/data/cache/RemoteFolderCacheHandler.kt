package com.noto.app.data.cache

import com.noto.app.data.GeneralFolderHandler
import com.noto.app.data.model.mapper.FolderMapper
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import kotlinx.coroutines.flow.first

class RemoteFolderCacheHandler(
    private val localFolderDataSource: LocalFolderDataSource,
    private val folderMapper: FolderMapper,
    private val generalFolderHandler: GeneralFolderHandler,
) : RemoteItemCacheHandler<RemoteFolder> {
    override suspend fun cacheRemoteItems(remoteItems: List<RemoteFolder>) {
        val remoteLocalFolders = remoteItems.map { remoteFolder -> folderMapper.mapRemoteFolderToLocalFolder(remoteFolder) }
        val isGeneralFolderCreated = remoteLocalFolders.any { remoteLocalFolder -> remoteLocalFolder.title.isBlank() }
        if (!isGeneralFolderCreated) generalFolderHandler.createGeneralFolder()
        remoteLocalFolders.forEach { remoteLocalFolder ->
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
                val databaseLocalFolder = localFolderDataSource.getLocalFolderByRemoteId(remoteLocalFolder.remoteId).first()
                if (databaseLocalFolder == null) {
                    localFolderDataSource.createLocalFolder(remoteLocalFolder.copy(id = RemoteItemWorker.NewItemId))
                } else {
                    localFolderDataSource.updateLocalFolder(remoteLocalFolder.copy(id = databaseLocalFolder.id))
                }
            }
        }
    }
}