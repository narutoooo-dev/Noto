package com.noto.app.data.label.source

import androidx.room.*
import com.noto.app.data.label.model.LocalEncryptedLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalEncryptedLabelDao : LocalEncryptedLabelDataSource {

    @Query("SELECT * FROM encrypted_labels")
    override fun getAllLocalEncryptedLabels(): Flow<List<LocalEncryptedLabel>>

    @Query("SELECT * FROM encrypted_labels WHERE folder_id = :localFolderId")
    override fun getLocalEncryptedLabelsByFolderId(localFolderId: Long): Flow<List<LocalEncryptedLabel>>

    @Query("SELECT * FROM encrypted_labels WHERE id = :localLabelId")
    override fun getLocalEncryptedLabelById(localLabelId: Long): Flow<LocalEncryptedLabel?>

    @Query("SELECT * FROM encrypted_labels WHERE remote_id = :remoteLabelId")
    override fun getLocalEncryptedLabelByRemoteId(remoteLabelId: String): Flow<LocalEncryptedLabel?>

    @Insert
    override suspend fun createLocalEncryptedLabel(localEncryptedLabel: LocalEncryptedLabel): Long

    @Update
    override suspend fun updateLocalEncryptedLabel(localEncryptedLabel: LocalEncryptedLabel)

    @Delete
    override suspend fun deleteLocalEncryptedLabel(localEncryptedLabel: LocalEncryptedLabel)

    @Query("DELETE FROM encrypted_labels")
    override suspend fun clearLocalEncryptedLabels()

}