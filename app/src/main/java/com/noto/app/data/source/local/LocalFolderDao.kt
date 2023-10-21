package com.noto.app.data.source.local

import androidx.room.*
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.domain.source.local.LocalFolderDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalFolderDao : LocalFolderDataSource {

    @Query("SELECT * FROM folders")
    override fun getAllLocalFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_vaulted = 0")
    override fun getAllUnvaultedLocalFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_archived = 0 AND is_vaulted = 0")
    override fun getLocalFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_archived = 1 AND is_vaulted = 0")
    override fun getArchivedLocalFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_vaulted = 1 AND is_archived = 0")
    override fun getVaultedLocalFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    override fun getLocalFolderById(folderId: Long): Flow<LocalFolder>

    @Query("SELECT * FROM folders WHERE remote_id = :remoteFolderId")
    override fun getLocalFolderByRemoteId(remoteFolderId: String): Flow<LocalFolder?>

    @Insert
    override suspend fun createLocalFolder(folder: LocalFolder): Long

    @Update
    override suspend fun updateLocalFolder(folder: LocalFolder)

    @Query("UPDATE folders SET remote_id = :remoteId WHERE id = :id")
    override suspend fun updateLocalFolderRemoteIdById(id: Long, remoteId: String)

    @Delete
    override suspend fun deleteLocalFolder(folder: LocalFolder)

    @Query("DELETE FROM folders")
    override suspend fun clearLocalFolders()
}