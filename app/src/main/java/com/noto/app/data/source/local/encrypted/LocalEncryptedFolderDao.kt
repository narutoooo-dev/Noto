package com.noto.app.data.source.local.encrypted

import androidx.room.*
import com.noto.app.data.model.local.encrypted.LocalEncryptedFolder
import com.noto.app.domain.source.local.encrypted.LocalEncryptedFolderDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalEncryptedFolderDao : LocalEncryptedFolderDataSource {

    @Query("SELECT * FROM encrypted_folders")
    override fun getAllLocalEncryptedFolders(): Flow<List<LocalEncryptedFolder>>

    @Query("SELECT * FROM encrypted_folders WHERE parent_id = :localFolderId")
    override fun getChildLocalEncryptedFolders(localFolderId: Long): Flow<List<LocalEncryptedFolder>>

    @Query("SELECT * FROM encrypted_folders WHERE id = :localFolderId")
    override fun getLocalEncryptedFolderById(localFolderId: Long): Flow<LocalEncryptedFolder?>

    @Query("SELECT COUNT(1) FROM encrypted_folders WHERE id = :localFolderId")
    override suspend fun checkIfLocalEncryptedFolderExistsById(localFolderId: Long): Boolean

    @Insert
    override suspend fun createLocalEncryptedFolder(localEncryptedFolder: LocalEncryptedFolder): Long

    @Update
    override suspend fun updateLocalEncryptedFolder(localEncryptedFolder: LocalEncryptedFolder)

    @Delete
    override suspend fun deleteLocalEncryptedFolder(localEncryptedFolder: LocalEncryptedFolder)

    @Query("DELETE FROM encrypted_folders")
    override suspend fun clearLocalEncryptedFolders()

}