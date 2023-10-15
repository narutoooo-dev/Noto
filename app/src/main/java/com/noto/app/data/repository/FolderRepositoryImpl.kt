package com.noto.app.data.repository

import com.noto.app.data.database.*
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.domain.model.Folder
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.source.local.LocalFolderDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class FolderRepositoryImpl(
    private val dataSource: LocalFolderDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> = dataSource.getAllFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(dispatcher)

    override fun getAllUnvaultedFolders(): Flow<List<Folder>> = dataSource.getAllUnvaultedFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(dispatcher)

    override fun getFolders(): Flow<List<Folder>> = dataSource.getFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(dispatcher)

    override fun getArchivedFolders(): Flow<List<Folder>> = dataSource.getArchivedFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(dispatcher)

    override fun getVaultedFolders(): Flow<List<Folder>> = dataSource.getVaultedFolders()
        .map { it.map { it.toDomainFolder() } }
        .flowOn(dispatcher)

    override fun getFolderById(folderId: Long): Flow<Folder> = dataSource.getFolderById(folderId)
        .filterNotNull()
        .map { it.toDomainFolder() }
        .flowOn(dispatcher)

    override suspend fun createFolder(folder: Folder, overridePosition: Boolean) = withContext(dispatcher) {
        val position = if (overridePosition) getFolderPosition() else folder.position
        dataSource.createFolder(folder.copy(position = position).toLocalFolder())
    }

    override suspend fun updateFolder(folder: Folder) = withContext(dispatcher) {
        dataSource.updateFolder(folder.toLocalFolder())
    }

    override suspend fun deleteFolder(folder: Folder) = withContext(dispatcher) {
        dataSource.deleteFolder(folder.toLocalFolder())
    }

    override suspend fun clearFolders() = withContext(dispatcher) {
        dataSource.clearFolders()
    }

    private suspend fun getFolderPosition() = withContext(dispatcher) {
        dataSource.getFolders()
            .filterNotNull()
            .first()
            .count()
    }

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

    private fun Folder.toLocalFolder(): LocalFolder {
        return LocalFolder(
            id = id,
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