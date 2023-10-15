package com.noto.app.domain.source.local

import com.noto.app.data.model.local.LocalFolder
import kotlinx.coroutines.flow.Flow

interface LocalFolderDataSource {

    fun getAllFolders(): Flow<List<LocalFolder>>

    fun getAllUnvaultedFolders(): Flow<List<LocalFolder>>

    fun getFolders(): Flow<List<LocalFolder>>

    fun getArchivedFolders(): Flow<List<LocalFolder>>

    fun getVaultedFolders(): Flow<List<LocalFolder>>

    fun getFolderById(folderId: Long): Flow<LocalFolder>

    suspend fun createFolder(folder: LocalFolder): Long

    suspend fun updateFolder(folder: LocalFolder)

    suspend fun deleteFolder(folder: LocalFolder)

    suspend fun clearFolders()
}