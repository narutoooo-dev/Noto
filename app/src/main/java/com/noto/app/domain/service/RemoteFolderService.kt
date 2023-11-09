package com.noto.app.domain.service

interface RemoteFolderService {

    fun getRemoteFolders()

    fun createRemoteFolder(remoteFolderId: String)

    fun createRemoteGeneralFolder()

    fun updateRemoteFolder(remoteFolderId: String)

    fun deleteRemoteFolder(remoteFolderId: String)

}