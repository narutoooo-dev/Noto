package com.noto.app.data.label.source

import com.noto.app.data.label.model.LocalEncryptedLabel
import kotlinx.coroutines.flow.Flow

interface LocalEncryptedLabelDataSource {

    fun getAllLocalEncryptedLabels(): Flow<List<LocalEncryptedLabel>>

    fun getLocalEncryptedLabelsByFolderId(localFolderId: Long): Flow<List<LocalEncryptedLabel>>

    fun getLocalEncryptedLabelById(localLabelId: Long): Flow<LocalEncryptedLabel?>

    fun getLocalEncryptedLabelByRemoteId(remoteLabelId: String): Flow<LocalEncryptedLabel?>

    suspend fun createLocalEncryptedLabel(localEncryptedLabel: LocalEncryptedLabel): Long

    suspend fun updateLocalEncryptedLabel(localEncryptedLabel: LocalEncryptedLabel)

    suspend fun deleteLocalEncryptedLabel(localEncryptedLabel: LocalEncryptedLabel)

    suspend fun clearLocalEncryptedLabels()

}