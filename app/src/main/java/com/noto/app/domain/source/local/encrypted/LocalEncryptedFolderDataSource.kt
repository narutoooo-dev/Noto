package com.noto.app.domain.source.local.encrypted

import com.noto.app.data.model.local.encrypted.LocalEncryptedFolder
import kotlinx.coroutines.flow.Flow

interface LocalEncryptedFolderDataSource {

    fun getAllLocalEncryptedFolders(): Flow<List<LocalEncryptedFolder>>

    fun getChildLocalEncryptedFolders(localFolderId: Long): Flow<List<LocalEncryptedFolder>>

    fun getLocalEncryptedFolderById(localFolderId: Long): Flow<LocalEncryptedFolder?>

    suspend fun createLocalEncryptedFolder(localEncryptedFolder: LocalEncryptedFolder): Long

    suspend fun updateLocalEncryptedFolder(localEncryptedFolder: LocalEncryptedFolder)

    suspend fun deleteLocalEncryptedFolder(localEncryptedFolder: LocalEncryptedFolder)

    suspend fun clearLocalEncryptedFolders()

}