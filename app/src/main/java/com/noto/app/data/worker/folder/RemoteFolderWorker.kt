package com.noto.app.data.worker.folder

import com.noto.app.crypto.EncryptionHandler
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import com.noto.app.util.KoinModules
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.get
import java.util.UUID

sealed interface RemoteFolderWorker : RemoteItemWorker {

    val localFolderDataSource get() = get<LocalFolderDataSource>()

    val remoteFolderDataSource get() = get<RemoteFolderDataSource>()

    private val encryptionHandler get() = get<EncryptionHandler>()

    private val json get() = get<Json>(KoinModules.Qualifiers.CryptoJson)

    fun RemoteFolder.toLocalFolder(): LocalFolder {
        val decryptedContent = encryptionHandler.decryptData(keyset, encryptedContent)
        val decodedContent = decryptedContent.decodeToString()
        val content = json.decodeFromString<LocalFolder>(decodedContent)
        return content.copy(id = 0L, remoteId = id.toString(), keyset = keyset)
    }

    fun LocalFolder.toRemoteFolder(): RemoteFolder {
        val jsonContent = json.encodeToString(this.copy(id = 0L))
        val encodedContent = jsonContent.encodeToByteArray()
        val encryptedContent = encryptionHandler.encryptData(keyset!!, encodedContent)
        return RemoteFolder(
            id = UUID.fromString(remoteId),
            keyset = keyset,
            encryptedContent = encryptedContent,
        )
    }

}