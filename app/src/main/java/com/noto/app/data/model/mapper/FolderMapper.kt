package com.noto.app.data.model.mapper

import com.noto.app.crypto.VaultEncryptionHandler
import com.noto.app.crypto.tink.TinkCryptoManager
import com.noto.app.crypto.tink.TinkEncryptionHandler
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.data.model.local.encrypted.LocalEncryptedFolder
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.model.Folder
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.encrypted.LocalEncryptedFolderDataSource
import com.noto.app.domain.source.local.encrypted.LocalEncryptedNoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import java.util.UUID

class FolderMapper(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val localEncryptedFolderDataSource: LocalEncryptedFolderDataSource,
    private val localEncryptedNoteDataSource: LocalEncryptedNoteDataSource,
    private val settingsRepository: SettingsRepository,
    private val tinkCryptoManager: TinkCryptoManager,
    private val tinkEncryptionHandler: TinkEncryptionHandler,
    private val propertyMapper: PropertyMapper,
    private val vaultEncryptionHandler: VaultEncryptionHandler,
) {

    suspend fun mapDomainFolderToLocalFolder(domainFolder: Folder, forceGenerateEncryptedKeyset: Boolean = false): LocalFolder {
        return with(domainFolder) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(id)
            val localFolder = getLocalFolderById(id, isLocalFolderVaulted)
            val remoteId = localFolder?.remoteId ?: UUID.randomUUID().toString()
            val keyset = localFolder?.getOrGenerateLocalFolderKeyset(forceGenerateEncryptedKeyset)
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
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(id)
            val parentFolder = getLocalFolderById(parentId, isLocalFolderVaulted)?.let { mapLocalFolderToDomainFolder(it) }
            val childFolders = getChildDomainFoldersById(id, isLocalFolderVaulted)
            val notesCount = countMainNotesByFolderId(id, isLocalFolderVaulted)
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
            folder.setAsParentForChildFolders()
        }
    }

    suspend fun mapLocalFolderToLocalEncryptedFolder(localFolder: LocalFolder): LocalEncryptedFolder {
        return with(localFolder) {
            LocalEncryptedFolder(
                id = id,
                remoteId = remoteId,
                keyset = keyset,
                parentId = parentId,
                content = vaultEncryptionHandler.encryptItem(this),
            )
        }
    }

    suspend fun mapLocalEncryptedFolderToLocalFolder(localEncryptedFolder: LocalEncryptedFolder): LocalFolder {
        return with(localEncryptedFolder) {
            vaultEncryptionHandler.decryptItem<LocalFolder>(content)
                .copy(
                    remoteId = remoteId,
                    keyset = keyset,
                )
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

    private suspend fun LocalFolder.getOrGenerateLocalFolderKeyset(forceGenerateEncryptedKeyset: Boolean): String? {
        return if (settingsRepository.isUserLoggedIn.first() || forceGenerateEncryptedKeyset) {
            keyset ?: tinkCryptoManager.keysetGenerator.generateEncryptedKeyset()
        } else {
            null
        }
    }

    suspend fun getLocalFolderById(folderId: Long?, isLocalFolderVaulted: Boolean): LocalFolder? {
        return folderId?.let { id ->
            if (isLocalFolderVaulted) {
                localEncryptedFolderDataSource.getLocalEncryptedFolderById(id).firstOrNull()
                    ?.let { mapLocalEncryptedFolderToLocalFolder(it) }
            } else {
                localFolderDataSource.getLocalFolderById(id).firstOrNull()
            }
        }
    }

    suspend fun getLocalFolderByRemoteId(remoteFolderId: String?, isLocalFolderVaulted: Boolean): LocalFolder? {
        return remoteFolderId?.let { id ->
            if (isLocalFolderVaulted) {
                localEncryptedFolderDataSource.getLocalEncryptedFolderByRemoteId(id).firstOrNull()
                    ?.let { mapLocalEncryptedFolderToLocalFolder(it) }
            } else {
                localFolderDataSource.getLocalFolderByRemoteId(id).firstOrNull()
            }
        }
    }

    private suspend fun getChildDomainFoldersById(folderId: Long, isLocalFolderVaulted: Boolean): List<Folder> {
        return if (isLocalFolderVaulted) {
            localEncryptedFolderDataSource.getChildLocalEncryptedFolders(folderId).first()
                .map { mapLocalEncryptedFolderToLocalFolder(it) }
        } else {
            localFolderDataSource.getChildLocalFolders(folderId).first()
        }.map { mapLocalFolderToDomainFolder(it.copy(parentId = null)) }
    }

    private suspend fun countMainNotesByFolderId(folderId: Long, isLocalFolderVaulted: Boolean): Int {
        return if (isLocalFolderVaulted) {
            localEncryptedNoteDataSource.countMainLocalEncryptedNotesByFolderId(folderId)
        } else {
            localNoteDataSource.countMainLocalNotesByFolderId(folderId)
        }.first()
    }

    private fun Folder.setAsParentForChildFolders() = copy(childFolders = childFolders.map { it.copy(parentFolder = this) })

}