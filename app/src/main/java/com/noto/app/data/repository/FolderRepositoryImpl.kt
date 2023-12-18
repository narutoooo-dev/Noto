package com.noto.app.data.repository

import com.noto.app.crypto.CryptoManager
import com.noto.app.data.model.DomainMappers
import com.noto.app.data.model.LocalMappers
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.data.model.local.LocalGeneralFolderManager
import com.noto.app.domain.model.Folder
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.service.RemoteFolderService
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import com.noto.app.util.isGeneral
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID

class FolderRepositoryImpl(
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val settingsRepository: SettingsRepository,
    private val remoteFolderService: RemoteFolderService,
    private val cryptoManager: CryptoManager,
    private val coroutineDispatcher: CoroutineDispatcher,
) : FolderRepository, LocalGeneralFolderManager {

    override fun getMainFolders(): Flow<List<Folder>> = flow {
        if (isUserLoggedIn()) remoteFolderService.getRemoteFolders()
        localFolderDataSource.getMainLocalFolders()
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
            if (isUserLoggedIn()) {
                remoteFolderService.createRemoteGeneralFolder()
            } else {
                val localGeneralFolder = newLocalGeneralFolder()
                localFolderDataSource.createLocalFolder(localGeneralFolder)
            }
        }
    }

    override suspend fun createFolder(folder: Folder): Result<Long> = runCatching {
        withContext(coroutineDispatcher) {
            val position = getFolderPosition()
            val positionedFolder = folder.copy(position = position, title = folder.correctTitle)
            val localFolder = positionedFolder.toLocalFolder()
            val localFolderId = localFolderDataSource.createLocalFolder(localFolder)
            if (isUserLoggedIn()) remoteFolderService.createRemoteFolder(localFolder.remoteId)
            localFolderId
        }
    }

    override suspend fun updateFolder(folder: Folder) = runCatching {
        withContext(coroutineDispatcher) {
            val localFolder = folder.copy(title = folder.correctTitle).toLocalFolder()
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
        localFolderDataSource.getMainLocalFolders()
            .filterNotNull()
            .first()
            .count()
    }

    private suspend fun isUserLoggedIn() = settingsRepository.isUserLoggedIn.first()

    private suspend fun LocalFolder.toDomainFolder(): Folder {
        val parentFolder = parentId?.let { localFolderDataSource.getLocalFolderById(parentId) }
            ?.firstOrNull()
            ?.toDomainFolder()

        val childFolders = localFolderDataSource.getChildLocalFolders(id)
            .first()
            .map { it.copy(parentId = null).toDomainFolder() }

        val notesCount = localNoteDataSource.countMainLocalNotesByFolderId(id).first()

        val folder = Folder(
            id = id,
            parentFolder = parentFolder,
            title = title,
            position = position,
            color = LocalMappers.NotoColor.map(color),
            creationDate = LocalMappers.Instant.map(creationDate),
            layout = LocalMappers.Layout.map(layout),
            notePreviewSize = notePreviewSize,
            isArchived = isArchived,
            isPinned = isPinned,
            isShowNoteCreationDate = isShowNoteCreationDate,
            newNoteCursorPosition = LocalMappers.NewNoteCursorPosition.map(newNoteCursorPosition),
            sortingType = LocalMappers.NoteListSortingType.map(sortingType),
            sortingOrder = LocalMappers.SortingOrder.map(sortingOrder),
            grouping = LocalMappers.Grouping.map(grouping),
            groupingOrder = LocalMappers.GroupingOrder.map(groupingOrder),
            isVaulted = isVaulted,
            scrollingPosition = scrollingPosition,
            filteringType = LocalMappers.FilteringType.map(filteringType),
            openNotesIn = LocalMappers.OpenNotesIn.map(openNotesIn),
            childFolders = childFolders,
            notesCount = notesCount,
        )

        // Required for swiping left/right to nest folders.
        return folder.copy(childFolders = childFolders.map { it.copy(parentFolder = folder) })
    }

    private suspend fun Folder.toLocalFolder(): LocalFolder {
        val localFolder = localFolderDataSource.getLocalFolderById(id).firstOrNull()
        val remoteId = localFolder?.remoteId ?: UUID.randomUUID().toString()
        val keyset = if (isUserLoggedIn()) {
            localFolder?.keyset ?: cryptoManager.generateKeyset()
        } else {
            null
        }
        return LocalFolder(
            id = id,
            remoteId = remoteId,
            parentId = parentFolder?.id,
            title = title,
            position = position,
            color = DomainMappers.NotoColor.map(color),
            creationDate = DomainMappers.Instant.map(creationDate),
            layout = DomainMappers.Layout.map(layout),
            notePreviewSize = notePreviewSize,
            isArchived = isArchived,
            isPinned = isPinned,
            isShowNoteCreationDate = isShowNoteCreationDate,
            newNoteCursorPosition = DomainMappers.NewNoteCursorPosition.map(newNoteCursorPosition),
            sortingType = DomainMappers.NoteListSortingType.map(sortingType),
            sortingOrder = DomainMappers.SortingOrder.map(sortingOrder),
            grouping = DomainMappers.Grouping.map(grouping),
            groupingOrder = DomainMappers.GroupingOrder.map(groupingOrder),
            isVaulted = isVaulted,
            scrollingPosition = scrollingPosition,
            filteringType = DomainMappers.FilteringType.map(filteringType),
            openNotesIn = DomainMappers.OpenNotesIn.map(openNotesIn),
            keyset = keyset
        )
    }

    @Suppress("DEPRECATION")
    private val Folder.correctTitle
        get() = if (isGeneral) "" else title

    override suspend fun newLocalGeneralFolder(): LocalFolder = Folder.General.toLocalFolder()

}