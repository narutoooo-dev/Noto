package com.noto.app.data.folder

interface RemoteFolderService {

    fun getRemoteFolders()

    fun createRemoteFolder(remoteFolderId: String)

    fun updateRemoteFolder(remoteFolderId: String)

    fun deleteRemoteFolder(remoteFolderId: String)

}