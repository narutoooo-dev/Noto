package com.noto.app.data.worker.folder

import com.noto.app.crypto.EncryptionHandler
import com.noto.app.data.model.local.LocalFolder
import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import org.koin.core.component.get
import java.util.UUID

sealed interface RemoteFolderWorker : RemoteItemWorker {

    val localFolderDataSource get() = get<LocalFolderDataSource>()

    val remoteFolderDataSource get() = get<RemoteFolderDataSource>()

    private val encryptionHandler get() = get<EncryptionHandler>()

    fun LocalFolder.toRemoteFolder(): RemoteFolder {
        val encryptedContent = encryptionHandler.encryptItem(keyset!!, this.copy(id = 0L))
        return RemoteFolder(
            id = UUID.fromString(remoteId),
            keyset = keyset,
            encryptedContent = encryptedContent,
        )
    }

    fun RemoteFolder.toLocalFolder(): LocalFolder {
        val decryptedContent = encryptionHandler.decryptItem<LocalFolder>(keyset, encryptedContent)
        return decryptedContent.copy(id = 0L, remoteId = id.toString(), keyset = keyset)
    }

}