package com.noto.app.data.repository

import com.noto.app.data.database.*
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.domain.model.Folder
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.service.RemoteFolderService
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID

class FolderRepositoryImpl(
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val localFolderDataSource: LocalFolderDataSource,
    private val settingsRepository: SettingsRepository,
    private val remoteFolderService: RemoteFolderService,
    private val coroutineDispatcher: CoroutineDispatcher,
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> = localFolderDataSource.getAllLocalFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(coroutineDispatcher)

    override fun getAllUnvaultedFolders(): Flow<List<Folder>> = localFolderDataSource.getAllUnvaultedLocalFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(coroutineDispatcher)

    override fun getFolders(): Flow<List<Folder>> = flow {
        if (isUserLoggedIn()) remoteFolderService.getRemoteFolders()
        localFolderDataSource.getLocalFolders()
            .map { it.map { it.toDomainFolder() } }
            .also { emitAll(it) }
    }.flowOn(coroutineDispatcher)

    override fun getArchivedFolders(): Flow<List<Folder>> = localFolderDataSource.getArchivedLocalFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(coroutineDispatcher)

    override fun getVaultedFolders(): Flow<List<Folder>> = localFolderDataSource.getVaultedLocalFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(coroutineDispatcher)

    override fun getFolderById(folderId: Long): Flow<Folder> = localFolderDataSource.getLocalFolderById(folderId)
        .filterNotNull()
        .map { it.toDomainFolder() }
        .flowOn(coroutineDispatcher)

    override suspend fun createGeneralFolder(): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val generalFolder = Folder.GeneralFolder()
            val localGeneralFolder = generalFolder.toLocalFolder()
            if (isUserLoggedIn()) {
                val isRemoteGeneralFolderCreated = remoteFolderDataSource.getRemoteGeneralFolderOrNull() != null
                if (!isRemoteGeneralFolderCreated) {
                    localFolderDataSource.createLocalFolder(localGeneralFolder)
                    remoteFolderService.createRemoteFolder(localGeneralFolder.remoteId)
                } // Else: General folder already exists, don't create it, but fetch it instead.
            } else {
                localFolderDataSource.createLocalFolder(localGeneralFolder)
            }
        }
    }

    override suspend fun createFolder(folder: Folder, overridePosition: Boolean): Result<Long> = runCatching {
        withContext(coroutineDispatcher) {
            val position = if (overridePosition) getFolderPosition() else folder.position
            val positionedFolder = folder.copy(position = position)
            val localFolder = positionedFolder.toLocalFolder()
            val localFolderId = localFolderDataSource.createLocalFolder(localFolder)
            if (isUserLoggedIn()) remoteFolderService.createRemoteFolder(localFolder.remoteId)
            localFolderId
        }
    }

    override suspend fun updateFolder(folder: Folder) = runCatching {
        withContext(coroutineDispatcher) {
            val localFolder = folder.toLocalFolder()
            localFolderDataSource.updateLocalFolder(localFolder)
            if (isUserLoggedIn()) remoteFolderService.updateRemoteFolder(localFolder.remoteId)
        }
    }

    override suspend fun deleteFolder(folder: Folder) = runCatching {
        withContext(coroutineDispatcher) {
            val localFolder = folder.toLocalFolder()
            localFolderDataSource.deleteLocalFolder(localFolder)
            if (isUserLoggedIn()) remoteFolderService.deleteRemoteFolder(localFolder.remoteId)
        }
    }

    override suspend fun clearFolders() = withContext(coroutineDispatcher) {
        localFolderDataSource.clearLocalFolders()
    }

    private suspend fun getFolderPosition() = withContext(coroutineDispatcher) {
        localFolderDataSource.getLocalFolders()
            .filterNotNull()
            .first()
            .count()
    }

    private suspend fun isUserLoggedIn() = settingsRepository.isUserLoggedIn.first()

    private fun LocalFolder.toDomainFolder(): Folder {
        return Folder(
            id = id,
            parentId = parentId,
            title = title,
            position = position,
            color = NotoColorConverter.toEnum(color),
            creationDate = InstantConverter.toDate(creationDate)!!,
            layout = LayoutConvertor.toEnum(layout),
            notePreviewSize = notePreviewSize,
            isArchived = isArchived,
            isPinned = isPinned,
            isShowNoteCreationDate = isShowNoteCreationDate,
            newNoteCursorPosition = NewNoteCursorPositionConvertor.toEnum(newNoteCursorPosition),
            sortingType = SortingTypeConverter.toEnum(sortingType),
            sortingOrder = SortingOrderConverter.toEnum(sortingOrder),
            grouping = GroupingConvertor.toEnum(grouping),
            groupingOrder = GroupingOrderConverter.toEnum(groupingOrder),
            isVaulted = isVaulted,
            scrollingPosition = scrollingPosition,
            filteringType = FilteringTypeConverter.toEnum(filteringType),
            openNotesIn = OpenNotesInConverter.toEnum(openNotesIn),
            folders = emptyList(),
        )
    }

    private suspend fun Folder.toLocalFolder(): LocalFolder {
        val remoteId = localFolderDataSource.getLocalFolderById(id).firstOrNull()?.remoteId ?: UUID.randomUUID().toString()
        return LocalFolder(
            id = id,
            remoteId = remoteId,
            parentId = parentId,
            title = title,
            position = position,
            color = NotoColorConverter.toOrdinal(color),
            creationDate = InstantConverter.toString(creationDate)!!,
            layout = LayoutConvertor.toOrdinal(layout),
            notePreviewSize = notePreviewSize,
            isArchived = isArchived,
            isPinned = isPinned,
            isShowNoteCreationDate = isShowNoteCreationDate,
            newNoteCursorPosition = NewNoteCursorPositionConvertor.toOrdinal(newNoteCursorPosition),
            sortingType = SortingTypeConverter.toOrdinal(sortingType),
            sortingOrder = SortingOrderConverter.toOrdinal(sortingOrder),
            grouping = GroupingConvertor.toOrdinal(grouping),
            groupingOrder = GroupingOrderConverter.toOrdinal(groupingOrder),
            isVaulted = isVaulted,
            scrollingPosition = scrollingPosition,
            filteringType = FilteringTypeConverter.toOrdinal(filteringType),
            openNotesIn = OpenNotesInConverter.toOrdinal(openNotesIn),
        )
    }
}