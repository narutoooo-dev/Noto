package com.noto.app.data.folder.source

import android.database.sqlite.SQLiteConstraintException
import androidx.room.*
import com.noto.app.data.folder.model.LocalFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalFolderDao : LocalFolderDataSource {

    @Query("SELECT * FROM folders")
    override fun getAllLocalFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_archived = 0 AND is_vaulted = 0 AND parent_id IS NULL")
    override fun getMainLocalFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE is_archived = 1 AND is_vaulted = 0 AND parent_id IS NULL")
    override fun getArchivedLocalFolders(): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE parent_id = :localFolderId")
    override fun getChildLocalFolders(localFolderId: Long): Flow<List<LocalFolder>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    override fun getLocalFolderById(folderId: Long): Flow<LocalFolder?>

    @Query("SELECT * FROM folders WHERE remote_id = :remoteFolderId")
    override fun getLocalFolderByRemoteId(remoteFolderId: String): Flow<LocalFolder?>

    @Query("SELECT COUNT(1) FROM folders WHERE id = :localFolderId")
    override suspend fun checkIfLocalFolderExistsById(localFolderId: Long): Boolean

    @Insert
    override suspend fun createLocalFolder(folder: LocalFolder): Long

    @Update
    override suspend fun updateLocalFolder(folder: LocalFolder)

    @Transaction
    override suspend fun upsertLocalFolder(folder: LocalFolder) {
        try {
            createLocalFolder(folder)
        } catch (_: SQLiteConstraintException) {
            updateLocalFolder(folder)
        }
    }

    @Query("UPDATE folders SET remote_id = :remoteId WHERE id = :id")
    override suspend fun updateLocalFolderRemoteIdById(id: Long, remoteId: String)

    @Delete
    override suspend fun deleteLocalFolder(folder: LocalFolder)

    @Query("DELETE FROM folders WHERE remote_id = :remoteFolderId")
    override suspend fun deleteLocalFolderByRemoteId(remoteFolderId: String)

    @Query("DELETE FROM folders")
    override suspend fun clearLocalFolders()

}