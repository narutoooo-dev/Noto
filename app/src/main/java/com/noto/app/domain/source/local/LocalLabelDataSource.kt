package com.noto.app.domain.source.local

import com.noto.app.data.model.local.LocalLabel
import kotlinx.coroutines.flow.Flow

interface LocalLabelDataSource {

    fun getAllLabels(): Flow<List<LocalLabel>>

    fun getLabelsByFolderId(folderId: Long): Flow<List<LocalLabel>>

    fun getLabelById(id: Long): Flow<LocalLabel>

    suspend fun createLabel(label: LocalLabel): Long

    suspend fun updateLabel(label: LocalLabel)

    suspend fun deleteLabel(label: LocalLabel)

    suspend fun clearLabels()

}