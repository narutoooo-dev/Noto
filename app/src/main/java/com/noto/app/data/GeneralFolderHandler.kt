package com.noto.app.data

import com.noto.app.data.folder.FolderMapper
import com.noto.app.data.folder.source.LocalFolderDataSource
import com.noto.app.data.folder.source.RemoteFolderDataSource
import com.noto.app.domain.folder.Folder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GeneralFolderHandler(
    private val localFolderDataSource: LocalFolderDataSource,
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val folderMapper: FolderMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) {
    suspend fun createGeneralFolder(): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val folder = Folder.General
            val localFolder = folderMapper.mapDomainFolderToLocalFolder(folder, forceGenerateEncryptedKeyset = true)
            val remoteFolder = folderMapper.mapLocalFolderToRemoteFolder(localFolder)
            localFolderDataSource.createLocalFolder(localFolder)
            remoteFolderDataSource.createRemoteFolder(remoteFolder)
        }
    }
}