package com.noto.app.data.source.local

import androidx.room.*
import com.noto.app.data.model.local.LocalLabel
import com.noto.app.domain.source.local.LocalLabelDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalLabelDao : LocalLabelDataSource {

    @Query("SELECT * FROM labels")
    override fun getAllLocalLabels(): Flow<List<LocalLabel>>

    @Query("SELECT labels.* FROM labels JOIN folders ON folders.id = labels.folder_id WHERE folders.is_archived = 0 AND folders.is_vaulted = 0")
    override fun getMainLocalLabels(): Flow<List<LocalLabel>>

    @Query("SELECT * FROM labels WHERE folder_id = :localFolderId")
    override fun getLocalLabelsByFolderId(localFolderId: Long): Flow<List<LocalLabel>>

    @Query("SELECT * FROM labels WHERE id = :localLabelId")
    override fun getLocalLabelById(localLabelId: Long): Flow<LocalLabel>

    @Query("SELECT * FROM labels WHERE remote_id = :remoteLabelId")
    override fun getLocalLabelByRemoteId(remoteLabelId: String): Flow<LocalLabel?>

    @Insert
    override suspend fun createLocalLabel(localLabel: LocalLabel): Long

    @Update
    override suspend fun updateLocalLabel(localLabel: LocalLabel)

    @Delete
    override suspend fun deleteLocalLabel(localLabel: LocalLabel)

    @Query("DELETE FROM labels WHERE remote_id = :remoteLabelId")
    override suspend fun deleteLocalLabelByRemoteId(remoteLabelId: String)

    @Query("DELETE FROM labels")
    override suspend fun clearLabels()

}