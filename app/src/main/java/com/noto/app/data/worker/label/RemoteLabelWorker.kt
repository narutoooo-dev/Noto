package com.noto.app.data.worker.label

import com.noto.app.crypto.EncryptionHandler
import com.noto.app.data.model.local.LocalLabel
import com.noto.app.data.model.remote.RemoteLabel
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.remote.RemoteLabelDataSource
import kotlinx.coroutines.flow.first
import org.koin.core.component.get
import java.util.UUID

sealed interface RemoteLabelWorker : RemoteItemWorker {

    val localLabelDataSource get() = get<LocalLabelDataSource>()

    val remoteLabelDataSource get() = get<RemoteLabelDataSource>()

    private val localFolderDataSource get() = get<LocalFolderDataSource>()

    private val encryptionHandler get() = get<EncryptionHandler>()

    suspend fun LocalLabel.toRemoteLabel(): RemoteLabel {
        val localFolder = localFolderDataSource.getLocalFolderById(folderId).first()!!
        val keyset = localFolder.keyset!!
        val encryptedContent = encryptionHandler.encryptItem(keyset, this.copy(id = 0L, folderId = 0L))
        return RemoteLabel(
            id = UUID.fromString(remoteId),
            folderId = UUID.fromString(localFolder.remoteId),
            encryptedContent = encryptedContent,
        )
    }

    suspend fun RemoteLabel.toLocalLabel(): LocalLabel {
        val localFolder = localFolderDataSource.getLocalFolderByRemoteId(folderId.toString()).first()!!
        val keyset = localFolder.keyset!!
        val decryptedContent = encryptionHandler.decryptItem<LocalLabel>(keyset, encryptedContent)
        return decryptedContent.copy(id = 0L, remoteId = id.toString(), folderId = localFolder.id)
    }

}