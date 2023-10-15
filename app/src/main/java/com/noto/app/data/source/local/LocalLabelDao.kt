package com.noto.app.data.source.local

import androidx.room.*
import com.noto.app.data.model.local.LocalLabel
import com.noto.app.domain.source.local.LocalLabelDataSource
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalLabelDao : LocalLabelDataSource {

    @Query("SELECT * FROM labels")
    override fun getAllLabels(): Flow<List<LocalLabel>>

    @Query("SELECT * FROM labels WHERE folder_id = :folderId")
    override fun getLabelsByFolderId(folderId: Long): Flow<List<LocalLabel>>

    @Query("SELECT * FROM labels WHERE id = :id")
    override fun getLabelById(id: Long): Flow<LocalLabel>

    @Insert
    override suspend fun createLabel(label: LocalLabel): Long

    @Update
    override suspend fun updateLabel(label: LocalLabel)

    @Delete
    override suspend fun deleteLabel(label: LocalLabel)

    @Query("DELETE FROM labels")
    override suspend fun clearLabels()

}