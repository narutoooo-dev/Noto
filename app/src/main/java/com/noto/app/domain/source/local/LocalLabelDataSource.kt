package com.noto.app.domain.source.local

import com.noto.app.data.model.local.LocalLabel
import kotlinx.coroutines.flow.Flow

interface LocalLabelDataSource {

    fun getAllLocalLabels(): Flow<List<LocalLabel>>

    fun getMainLocalLabels(): Flow<List<LocalLabel>>

    fun getLocalLabelsByFolderId(localFolderId: Long): Flow<List<LocalLabel>>

    fun getLocalLabelById(localLabelId: Long): Flow<LocalLabel>

    fun getLocalLabelByRemoteId(remoteLabelId: String): Flow<LocalLabel?>

    suspend fun createLocalLabel(localLabel: LocalLabel): Long

    suspend fun updateLocalLabel(localLabel: LocalLabel)

    suspend fun deleteLocalLabel(localLabel: LocalLabel)

    suspend fun clearLabels()

}