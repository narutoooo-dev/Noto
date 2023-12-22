package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteFolder

interface RemoteFolderDataSource {

    suspend fun getAllRemoteFolders(): List<RemoteFolder>

    suspend fun createRemoteFolder(remoteFolder: RemoteFolder)

    suspend fun updateRemoteFolder(remoteFolder: RemoteFolder)

    suspend fun deleteRemoteFolderById(remoteFolderId: String)

}