package com.noto.app.data.label

interface RemoteLabelService {

    fun getRemoteLabelsByFolderId(remoteFolderId: String)

    fun createRemoteLabel(remoteLabelId: String)

    fun updateRemoteLabel(remoteLabelId: String)

    fun deleteRemoteLabel(remoteLabelId: String)

}