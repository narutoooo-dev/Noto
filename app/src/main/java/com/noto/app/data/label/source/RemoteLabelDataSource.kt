package com.noto.app.data.label.source

import com.noto.app.data.label.model.RemoteLabel
import kotlinx.coroutines.flow.Flow

interface RemoteLabelDataSource {

    suspend fun getAllRemoteLabels(): List<RemoteLabel>

    suspend fun getRemoteLabelsSince(timestamp: String): List<RemoteLabel>

    suspend fun getRemoteLabelsByFolderId(remoteFolderId: String): List<RemoteLabel>

    suspend fun createRemoteLabel(remoteLabel: RemoteLabel)

    suspend fun updateRemoteLabel(remoteLabel: RemoteLabel)

    suspend fun deleteRemoteLabelById(remoteLabelId: String)

    suspend fun subscribeToRemoteLabelListeners()

    suspend fun unsubscribeToRemoteLabelListeners()

    suspend fun createRemoteLabelListener(): Flow<RemoteLabel>

    suspend fun updateRemoteLabelListener(): Flow<RemoteLabel>

    suspend fun deleteRemoteLabelListener(): Flow<String>

}