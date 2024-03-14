package com.noto.app.data.model.mapper

import com.noto.app.crypto.tink.TinkCryptoManager
import com.noto.app.crypto.tink.TinkEncryptionHandler
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.model.Folder
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import java.util.UUID

class FolderMapper(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val settingsRepository: SettingsRepository,
    private val tinkCryptoManager: TinkCryptoManager,
    private val tinkEncryptionHandler: TinkEncryptionHandler,
    private val propertyMapper: PropertyMapper,
) {

    suspend fun mapDomainFolderToLocalFolder(domainFolder: Folder, forceGenerateEncryptedKeyset: Boolean = false): LocalFolder {
        return with(domainFolder) {
            val localFolder = localFolderDataSource.getLocalFolderById(id).firstOrNull()
            val remoteId = localFolder?.remoteId ?: UUID.randomUUID().toString()
            val keyset = if (settingsRepository.isUserLoggedIn.first() || forceGenerateEncryptedKeyset) {
                localFolder?.keyset ?: tinkCryptoManager.keysetGenerator.generateEncryptedKeyset()
            } else {
                null
            }
            LocalFolder(
                id = id,
                remoteId = remoteId,
                parentId = parentFolder?.id,
                title = title,
                position = position,
                color = propertyMapper.mapDomainNotoColorToLocalNotoColor(color),
                creationDate = propertyMapper.mapDomainInstantToLocalInstant(creationDate),
                layout = propertyMapper.mapDomainLayoutToLocalLayout(layout),
                notePreviewSize = notePreviewSize,
                isArchived = isArchived,
                isPinned = isPinned,
                isShowNoteCreationDate = isShowNoteCreationDate,
                newNoteCursorPosition = propertyMapper.mapDomainNewNoteCursorPositionToLocalNewNoteCursorPosition(newNoteCursorPosition),
                sortingType = propertyMapper.mapDomainNoteListSortingTypeToLocalNoteListSortingType(sortingType),
                sortingOrder = propertyMapper.mapDomainSortingOrderToLocalSortingOrder(sortingOrder),
                grouping = propertyMapper.mapDomainGroupingToLocalGrouping(grouping),
                groupingOrder = propertyMapper.mapDomainGroupingOrderToLocalGroupingOrder(groupingOrder),
                isVaulted = isVaulted,
                scrollingPosition = scrollingPosition,
                filteringType = propertyMapper.mapDomainFilteringTypeToLocalFilteringType(filteringType),
                openNotesIn = propertyMapper.mapDomainOpenNotesInToLocalOpenNotesIn(openNotesIn),
                keyset = keyset
            )
        }
    }

    suspend fun mapLocalFolderToDomainFolder(localFolder: LocalFolder): Folder {
        return with(localFolder) {

            val parentFolder = parentId?.let { localFolderDataSource.getLocalFolderById(parentId) }
                ?.firstOrNull()
                ?.let { mapLocalFolderToDomainFolder(it) }

            val childFolders = localFolderDataSource.getChildLocalFolders(id)
                .first()
                .map { mapLocalFolderToDomainFolder(it.copy(parentId = null)) }

            val notesCount = localNoteDataSource.countMainLocalNotesByFolderId(id).first()

            val folder = Folder(
                id = id,
                parentFolder = parentFolder,
                title = title,
                position = position,
                color = propertyMapper.mapLocalNotoColorToDomainNotoColor(color),
                creationDate = propertyMapper.mapLocalInstantToDomainInstant(creationDate),
                layout = propertyMapper.mapLocalLayoutToDomainLayout(layout),
                notePreviewSize = notePreviewSize,
                isArchived = isArchived,
                isPinned = isPinned,
                isShowNoteCreationDate = isShowNoteCreationDate,
                newNoteCursorPosition = propertyMapper.mapLocalNewNoteCursorPositionToDomainNewNoteCursorPosition(newNoteCursorPosition),
                sortingType = propertyMapper.mapLocalNoteListSortingTypeToDomainNoteListSortingType(sortingType),
                sortingOrder = propertyMapper.mapLocalSortingOrderToDomainSortingOrder(sortingOrder),
                grouping = propertyMapper.mapLocalGroupingToDomainGrouping(grouping),
                groupingOrder = propertyMapper.mapLocalGroupingOrderToDomainGroupingOrder(groupingOrder),
                isVaulted = isVaulted,
                scrollingPosition = scrollingPosition,
                filteringType = propertyMapper.mapLocalFilteringTypeToDomainFilteringType(filteringType),
                openNotesIn = propertyMapper.mapLocalOpenNotesInToDomainOpenNotesIn(openNotesIn),
                childFolders = childFolders,
                notesCount = notesCount,
            )

            // Required for swiping left/right to nest folders.
            folder.copy(childFolders = childFolders.map { it.copy(parentFolder = folder) })
        }
    }

    suspend fun mapLocalFolderToRemoteFolder(localFolder: LocalFolder): RemoteFolder {
        return with(localFolder) {
            val encryptedContent = tinkEncryptionHandler.encryptItem(keyset!!, this.copy(id = 0L))
            RemoteFolder(
                id = UUID.fromString(remoteId),
                keyset = keyset,
                encryptedContent = encryptedContent,
                metaData = RemoteFolder.MetaData(updatedAt = Clock.System.now().toString()),
            )
        }
    }

    suspend fun mapRemoteFolderToLocalFolder(remoteFolder: RemoteFolder): LocalFolder {
        return with(remoteFolder) {
            val decryptedContent = tinkEncryptionHandler.decryptItem<LocalFolder>(keyset, encryptedContent)
            decryptedContent.copy(id = 0L, remoteId = id.toString(), keyset = keyset)
        }
    }

}