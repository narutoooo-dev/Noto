package com.noto.app.settings.backup

interface LocalBackupHandler {

    fun validateUri(uri: String?): Boolean

    suspend fun export(uri: String?, data: String, deleteCurrent: Boolean): Result<Unit>

    suspend fun import(uri: String?): Result<String>

    companion object {
        const val FileName = "Noto.json"
        const val OldFileName = "Old-Noto.json"
        const val JsonFileType = "application/json"
        const val OctetStreamFileType = "application/octet-stream"
        val FileTypes = arrayOf(JsonFileType, OctetStreamFileType)
    }

}