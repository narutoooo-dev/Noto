package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteFolder
import kotlinx.coroutines.flow.Flow

interface RemoteFolderDataSource {

    suspend fun getAllRemoteFolders(): List<RemoteFolder>

    suspend fun getRemoteFoldersSince(timestamp: String): List<RemoteFolder>

    suspend fun createRemoteFolder(remoteFolder: RemoteFolder)

    suspend fun updateRemoteFolder(remoteFolder: RemoteFolder)

    suspend fun deleteRemoteFolderById(remoteFolderId: String)

    suspend fun subscribeToRemoteFolderListeners()

    suspend fun unsubscribeToRemoteFolderListeners()

    suspend fun createRemoteFolderListener(): Flow<RemoteFolder>

    suspend fun updateRemoteFolderListener(): Flow<RemoteFolder>

    suspend fun deleteRemoteFolderListener(): Flow<String>

}