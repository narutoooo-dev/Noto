package com.noto.app.domain.vault

interface VaultRepository {

    suspend fun addFolderToVault(folderId: Long, nullifyParentFolder: Boolean = true): Result<Unit>

    suspend fun removeFolderFromVault(folderId: Long, nullifyParentFolder: Boolean = true): Result<Unit>

}