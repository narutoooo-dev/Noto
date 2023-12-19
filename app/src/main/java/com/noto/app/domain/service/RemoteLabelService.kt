package com.noto.app.domain.service

interface RemoteLabelService {

    fun getRemoteLabelsByFolderId(remoteFolderId: String)

    fun createRemoteLabel(remoteLabelId: String)

    fun updateRemoteLabel(remoteLabelId: String)

    fun deleteRemoteLabel(remoteLabelId: String)

}