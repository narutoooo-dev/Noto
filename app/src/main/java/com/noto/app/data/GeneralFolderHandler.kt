package com.noto.app.data

import com.noto.app.data.model.mapper.FolderMapper
import com.noto.app.domain.model.Folder
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
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