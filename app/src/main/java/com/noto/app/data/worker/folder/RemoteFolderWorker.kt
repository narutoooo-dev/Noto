package com.noto.app.data.worker.folder

import com.noto.app.crypto.EncryptionHandler
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import com.noto.app.util.CoroutineDispatcherQualifier
import com.noto.app.util.CryptoJsonQualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.UUID

sealed interface RemoteFolderWorker : KoinComponent {

    val coroutineDispatcher get() = get<CoroutineDispatcher>(CoroutineDispatcherQualifier)

    val localFolderDataSource get() = get<LocalFolderDataSource>()

    val remoteFolderDataSource get() = get<RemoteFolderDataSource>()

    val encryptionHandler get() = get<EncryptionHandler>()

    private val json get() = get<Json>(CryptoJsonQualifier)

    companion object {
        const val RemoteFolderId = "remote_folder_id"
    }

    fun RemoteFolder.toLocalFolder(): LocalFolder {
        val decryptedContent = encryptionHandler.decryptData(encryptedKey, encryptedContent)
        val decodedContent = decryptedContent.decodeToString()
        val content = json.decodeFromString<LocalFolder>(decodedContent)
        return content.copy(id = 0L, remoteId = id.toString(), encryptedKey = encryptedKey)
    }

    fun LocalFolder.toRemoteFolder(): RemoteFolder {
        val jsonContent = json.encodeToString(this.copy(id = 0L))
        val encodedContent = jsonContent.encodeToByteArray()
        val encryptedContent = encryptionHandler.encryptData(encryptedKey!!, encodedContent)
        return RemoteFolder(
            id = UUID.fromString(remoteId),
            encryptedKey = encryptedKey,
            encryptedContent = encryptedContent,
        )
    }

}