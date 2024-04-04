package com.noto.app.data.folder.source

import com.noto.app.data.folder.model.LocalFolder
import kotlinx.coroutines.flow.Flow

interface LocalFolderDataSource {

    fun getAllLocalFolders(): Flow<List<LocalFolder>>

    fun getMainLocalFolders(): Flow<List<LocalFolder>>

    fun getArchivedLocalFolders(): Flow<List<LocalFolder>>

    fun getChildLocalFolders(localFolderId: Long): Flow<List<LocalFolder>>

    fun getLocalFolderById(folderId: Long): Flow<LocalFolder?>

    fun getLocalFolderByRemoteId(remoteFolderId: String): Flow<LocalFolder?>

    suspend fun checkIfLocalFolderExistsById(localFolderId: Long): Boolean

    suspend fun createLocalFolder(folder: LocalFolder): Long

    suspend fun updateLocalFolder(folder: LocalFolder)

    suspend fun upsertLocalFolder(folder: LocalFolder)

    suspend fun updateLocalFolderRemoteIdById(id: Long, remoteId: String)

    suspend fun deleteLocalFolder(folder: LocalFolder)

    suspend fun deleteLocalFolderByRemoteId(remoteFolderId: String)

    suspend fun clearLocalFolders()

}