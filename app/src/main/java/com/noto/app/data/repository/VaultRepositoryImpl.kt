package com.noto.app.data.repository

import com.noto.app.data.model.local.LocalFolder
import com.noto.app.data.model.mapper.FolderMapper
import com.noto.app.data.model.mapper.NoteMapper
import com.noto.app.domain.repository.VaultRepository
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.encrypted.LocalEncryptedFolderDataSource
import com.noto.app.domain.source.local.encrypted.LocalEncryptedNoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VaultRepositoryImpl(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val localEncryptedFolderDataSource: LocalEncryptedFolderDataSource,
    private val localEncryptedNoteDataSource: LocalEncryptedNoteDataSource,
    private val folderMapper: FolderMapper,
    private val noteMapper: NoteMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) : VaultRepository {

    override suspend fun addFolderToVault(folderId: Long, nullifyParentFolder: Boolean): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val localFolder = localFolderDataSource.getLocalFolderById(folderId).first()!!.markAsVaulted(nullifyParentFolder)
            val localNotes = localNoteDataSource.getMainLocalNotesByFolderId(folderId).first()
            val localArchivedNotes = localNoteDataSource.getArchivedLocalNotesByFolderId(folderId).first()

            val localEncryptedFolder = folderMapper.mapLocalFolderToLocalEncryptedFolder(localFolder)
            val localEncryptedNotes = localNotes.map { noteMapper.mapLocalNoteToLocalEncryptedNote(it) }
            val localEncryptedArchivedNotes = localArchivedNotes.map { noteMapper.mapLocalNoteToLocalEncryptedNote(it) }
            val allLocalEncryptedNotes = localEncryptedNotes + localEncryptedArchivedNotes

            localEncryptedFolderDataSource.createLocalEncryptedFolder(localEncryptedFolder)
            allLocalEncryptedNotes.forEach { launch { localEncryptedNoteDataSource.createLocalEncryptedNote(it) } }

            localFolderDataSource.getChildLocalFolders(folderId).first()
                .map { it.id }
                .forEach { childFolderId -> addFolderToVault(childFolderId, nullifyParentFolder = false).getOrThrow() }

            localFolderDataSource.deleteLocalFolder(localFolder)
        }
    }

    override suspend fun removeFolderFromVault(folderId: Long, nullifyParentFolder: Boolean): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val localEncryptedFolder = localEncryptedFolderDataSource.getLocalEncryptedFolderById(folderId).first()!!
            val localEncryptedNotes = localEncryptedNoteDataSource.getMainLocalEncryptedNotesByFolderId(folderId).first()
            val localArchivedEncryptedNotes = localEncryptedNoteDataSource.getArchivedLocalEncryptedNotesByFolderId(folderId).first()

            val localFolder = folderMapper.mapLocalEncryptedFolderToLocalFolder(localEncryptedFolder).markAsUnvaulted(nullifyParentFolder)
            val localNotes = localEncryptedNotes.map { noteMapper.mapLocalEncryptedNoteToLocalNote(it) }
            val localArchivedNotes = localArchivedEncryptedNotes.map { noteMapper.mapLocalEncryptedNoteToLocalNote(it) }
            val allLocalNotes = localNotes + localArchivedNotes

            localFolderDataSource.createLocalFolder(localFolder)
            allLocalNotes.forEach { launch { localNoteDataSource.createLocalNote(it) } }

            localEncryptedFolderDataSource.getChildLocalEncryptedFolders(folderId).first()
                .map { it.id }
                .forEach { childFolderId -> removeFolderFromVault(childFolderId, nullifyParentFolder = false).getOrThrow() }

            localEncryptedFolderDataSource.deleteLocalEncryptedFolder(localEncryptedFolder)
        }
    }

    private fun LocalFolder.markAsVaulted(nullifyParentId: Boolean): LocalFolder =
        copy(isVaulted = true, isArchived = false, parentId = if (nullifyParentId) null else parentId)

    private fun LocalFolder.markAsUnvaulted(nullifyParentId: Boolean): LocalFolder =
        copy(isVaulted = false, isArchived = false, parentId = if (nullifyParentId) null else parentId)

}