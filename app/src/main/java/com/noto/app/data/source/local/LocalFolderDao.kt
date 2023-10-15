package com.noto.app.data.source.local

import androidx.room.*
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.domain.source.local.LocalFolderDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalFolderDao : LocalFolderDataSource {

    @Query("SELECT * FROM folders")
    override fun getAllFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_vaulted = 0")
    override fun getAllUnvaultedFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_archived = 0 AND is_vaulted = 0")
    override fun getFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_archived = 1 AND is_vaulted = 0")
    override fun getArchivedFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_vaulted = 1 AND is_archived = 0")
    override fun getVaultedFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    override fun getFolderById(folderId: Long): Flow<LocalFolder>

    @Insert
    override suspend fun createFolder(folder: LocalFolder): Long

    @Update
    override suspend fun updateFolder(folder: LocalFolder)

    @Delete
    override suspend fun deleteFolder(folder: LocalFolder)

    @Query("DELETE FROM folders")
    override suspend fun clearFolders()
}