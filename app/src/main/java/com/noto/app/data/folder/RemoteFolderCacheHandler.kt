package com.noto.app.data.folder

import com.noto.app.data.GeneralFolderHandler
import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.folder.model.RemoteFolder
import com.noto.app.data.folder.source.LocalFolderDataSource
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