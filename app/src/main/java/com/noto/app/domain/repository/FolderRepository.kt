package com.noto.app.domain.repository

import com.noto.app.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    fun getMainFolders(): Flow<List<Folder>>

    fun getArchivedFolders(): Flow<List<Folder>>

    fun getVaultedFolders(): Flow<List<Folder>>

    fun getFolderById(folderId: Long): Flow<Folder>

    suspend fun createGeneralFolder(): Result<Unit>

    suspend fun createFolder(folder: Folder): Result<Long>

    suspend fun updateFolder(folder: Folder): Result<Unit>

    suspend fun deleteFolder(folder: Folder): Result<Unit>

    suspend fun clearFolders()

}