package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteLabel

interface RemoteLabelDataSource {

    suspend fun getRemoteLabelsByFolderId(remoteFolderId: String): List<RemoteLabel>

    suspend fun createRemoteLabel(remoteLabel: RemoteLabel)

    suspend fun updateRemoteLabel(remoteLabel: RemoteLabel)

    suspend fun deleteRemoteLabelById(remoteLabelId: String)

}