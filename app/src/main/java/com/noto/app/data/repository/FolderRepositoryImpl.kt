package com.noto.app.data.repository

import com.noto.app.data.model.mapper.FolderMapper
import com.noto.app.domain.model.Folder
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.service.RemoteFolderService
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import com.noto.app.util.isGeneral
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class FolderRepositoryImpl(
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val localFolderDataSource: LocalFolderDataSource,
    private val settingsRepository: SettingsRepository,
    private val remoteFolderService: RemoteFolderService,
    private val folderMapper: FolderMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) : FolderRepository {

    override fun getMainFolders(): Flow<List<Folder>> = localFolderDataSource.getMainLocalFolders()
        .map { it.map { folderMapper.mapLocalFolderToDomainFolder(it) } }
        .flowOn(coroutineDispatcher)

    override fun getArchivedFolders(): Flow<List<Folder>> = localFolderDataSource.getArchivedLocalFolders()
        .map { it.map { folderMapper.mapLocalFolderToDomainFolder(it) } }
        .flowOn(coroutineDispatcher)

    override fun getVaultedFolders(): Flow<List<Folder>> = localFolderDataSource.getVaultedLocalFolders()
        .map { it.map { folderMapper.mapLocalFolderToDomainFolder(it) } }
        .flowOn(coroutineDispatcher)

    override fun getFolderById(folderId: Long): Flow<Folder> = localFolderDataSource.getLocalFolderById(folderId)
        .filterNotNull()
        .map { folderMapper.mapLocalFolderToDomainFolder(it) }
        .flowOn(coroutineDispatcher)

    override suspend fun createGeneralFolder(): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            if (!isUserLoggedIn()) {
                val folder = Folder.General
                val localFolder = folderMapper.mapDomainFolderToLocalFolder(folder)
                localFolderDataSource.createLocalFolder(localFolder)
            }
        }
    }

    override suspend fun createFolder(folder: Folder): Result<Long> = runCatching {
        withContext(coroutineDispatcher) {
            val position = getFolderPosition()
            val positionedFolder = folder.copy(position = position, title = folder.correctTitle)
            val localFolder = folderMapper.mapDomainFolderToLocalFolder(positionedFolder)
            val localFolderId = localFolderDataSource.createLocalFolder(localFolder)
            if (isUserLoggedIn()) remoteFolderService.createRemoteFolder(localFolder.remoteId)
            localFolderId
        }
    }

    override suspend fun updateFolder(folder: Folder) = runCatching {
        withContext(coroutineDispatcher) {
            val localFolder = folderMapper.mapDomainFolderToLocalFolder(folder.copy(title = folder.correctTitle))
            localFolderDataSource.updateLocalFolder(localFolder)
            if (isUserLoggedIn()) remoteFolderService.updateRemoteFolder(localFolder.remoteId)
        }
    }

    override suspend fun deleteFolder(folder: Folder) = runCatching {
        withContext(coroutineDispatcher) {
            val localFolder = folderMapper.mapDomainFolderToLocalFolder(folder)
            localFolderDataSource.deleteLocalFolder(localFolder)
            if (isUserLoggedIn()) remoteFolderService.deleteRemoteFolder(localFolder.remoteId)
        }
    }

    override suspend fun clearFolders() = withContext(coroutineDispatcher) {
        localFolderDataSource.clearLocalFolders()
    }

    private suspend fun getFolderPosition() = withContext(coroutineDispatcher) {
        localFolderDataSource.getMainLocalFolders()
            .filterNotNull()
            .first()
            .count()
    }

    private suspend fun isUserLoggedIn() = settingsRepository.isUserLoggedIn.first()

    @Suppress("DEPRECATION")
    private val Folder.correctTitle
        get() = if (isGeneral) "" else title

}