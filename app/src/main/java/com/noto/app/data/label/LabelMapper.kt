package com.noto.app.data.label

import com.noto.app.crypto.VaultEncryptionHandler
import com.noto.app.crypto.tink.TinkEncryptionHandler
import com.noto.app.data.PropertyMapper
import com.noto.app.data.folder.FolderMapper
import com.noto.app.data.folder.source.LocalEncryptedFolderDataSource
import com.noto.app.data.folder.source.LocalFolderDataSource
import com.noto.app.data.label.model.LocalEncryptedLabel
import com.noto.app.data.label.model.LocalLabel
import com.noto.app.data.label.model.RemoteLabel
import com.noto.app.data.label.source.LocalEncryptedLabelDataSource
import com.noto.app.data.label.source.LocalLabelDataSource
import com.noto.app.domain.label.Label
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import java.util.UUID

class LabelMapper(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val localEncryptedFolderDataSource: LocalEncryptedFolderDataSource,
    private val localEncryptedLabelDataSource: LocalEncryptedLabelDataSource,
    private val tinkEncryptionHandler: TinkEncryptionHandler,
    private val folderMapper: FolderMapper,
    private val propertyMapper: PropertyMapper,
    private val vaultEncryptionHandler: VaultEncryptionHandler,
) {

    suspend fun mapDomainLabelToLocalLabel(domainLabel: Label): LocalLabel {
        return with(domainLabel) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(folderId)
            val remoteId = getLocalLabelById(id, isLocalFolderVaulted)?.remoteId ?: UUID.randomUUID().toString()
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

    suspend fun mapLocalLabelToLocalEncryptedLabel(localLabel: LocalLabel): LocalEncryptedLabel {
        return with(localLabel) {
            LocalEncryptedLabel(
                id = id,
                remoteId = remoteId,
                folderId = folderId,
                content = vaultEncryptionHandler.encryptItem(this),
            )
        }
    }

    suspend fun mapLocalEncryptedLabelToLocalLabel(localEncryptedLabel: LocalEncryptedLabel): LocalLabel {
        return with(localEncryptedLabel) {
            vaultEncryptionHandler.decryptItem<LocalLabel>(content).copy(remoteId = remoteId)
        }
    }

    suspend fun mapLocalLabelToRemoteLabel(localLabel: LocalLabel): RemoteLabel {
        return with(localLabel) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(folderId)
            val localFolder = folderMapper.getLocalFolderById(folderId, isLocalFolderVaulted)!!
            val keyset = localFolder.keyset!!
            val encryptedContent = tinkEncryptionHandler.encryptItem(keyset, this.copy(id = 0L, folderId = 0L))
            RemoteLabel(
                id = UUID.fromString(remoteId),
                folderId = UUID.fromString(localFolder.remoteId),
                encryptedContent = encryptedContent,
                metaData = RemoteLabel.MetaData(updatedAt = Clock.System.now().toString()),
            )
        }
    }

    suspend fun mapRemoteLabelToLocalLabel(remoteLabel: RemoteLabel): LocalLabel {
        return with(remoteLabel) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsByRemoteId(folderId.toString())
            val localFolder = folderMapper.getLocalFolderByRemoteId(folderId.toString(), isLocalFolderVaulted)!!
            val keyset = localFolder.keyset!!
            val decryptedContent = tinkEncryptionHandler.decryptItem<LocalLabel>(keyset, encryptedContent)
            decryptedContent.copy(id = 0L, remoteId = id.toString(), folderId = localFolder.id)
        }
    }

    private suspend fun getLocalLabelById(labelId: Long?, isLocalFolderVaulted: Boolean): LocalLabel? {
        return labelId?.let { id ->
            if (isLocalFolderVaulted) {
                localEncryptedLabelDataSource.getLocalEncryptedLabelById(id).firstOrNull()
                    ?.let { mapLocalEncryptedLabelToLocalLabel(it) }
            } else {
                localLabelDataSource.getLocalLabelById(id).firstOrNull()
            }
        }
    }

}