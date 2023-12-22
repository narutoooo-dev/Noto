package com.noto.app.data.model.mapper

import com.noto.app.crypto.EncryptionHandler
import com.noto.app.data.model.local.LocalLabel
import com.noto.app.data.model.remote.RemoteLabel
import com.noto.app.domain.model.Label
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalLabelDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class LabelMapper(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val encryptionHandler: EncryptionHandler,
    private val propertyMapper: PropertyMapper,
) {

    suspend fun mapDomainLabelToLocalLabel(domainLabel: Label): LocalLabel {
        return with(domainLabel) {
            val localLabel = localLabelDataSource.getLocalLabelById(id).firstOrNull()
            val remoteId = localLabel?.remoteId ?: UUID.randomUUID().toString()
            LocalLabel(
                id = id,
                remoteId = remoteId,
                folderId = folderId,
                title = title,
                color = propertyMapper.mapDomainNotoColorToLocalNotoColor(color),
                position = position,
            )
        }
    }

    suspend fun mapLocalLabelToDomainLabel(localLabel: LocalLabel): Label {
        return with(localLabel) {
            Label(
                id = id,
                folderId = folderId,
                title = title,
                color = propertyMapper.mapLocalNotoColorToDomainNotoColor(color),
                position = position,
            )
        }
    }

    suspend fun mapLocalLabelToRemoteLabel(localLabel: LocalLabel): RemoteLabel {
        return with(localLabel) {
            val localFolder = localFolderDataSource.getLocalFolderById(folderId).first()!!
            val keyset = localFolder.keyset!!
            val encryptedContent = encryptionHandler.encryptItem(keyset, this.copy(id = 0L, folderId = 0L))
            RemoteLabel(
                id = UUID.fromString(remoteId),
                folderId = UUID.fromString(localFolder.remoteId),
                encryptedContent = encryptedContent,
            )
        }
    }

    suspend fun mapRemoteLabelToLocalLabel(remoteLabel: RemoteLabel): LocalLabel {
        return with(remoteLabel) {
            val localFolder = localFolderDataSource.getLocalFolderByRemoteId(folderId.toString()).first()!!
            val keyset = localFolder.keyset!!
            val decryptedContent = encryptionHandler.decryptItem<LocalLabel>(keyset, encryptedContent)
            decryptedContent.copy(id = 0L, remoteId = id.toString(), folderId = localFolder.id)
        }
    }

}