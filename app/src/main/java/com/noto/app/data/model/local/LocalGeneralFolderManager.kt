package com.noto.app.data.model.local

fun interface LocalGeneralFolderManager {

    suspend fun newLocalGeneralFolder(): LocalFolder

}