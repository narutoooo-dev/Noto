package com.noto.app.data.label

import com.noto.app.data.folder.source.LocalEncryptedFolderDataSource
import com.noto.app.data.label.source.LocalEncryptedLabelDataSource
import com.noto.app.data.label.source.LocalLabelDataSource
import com.noto.app.data.label.source.RemoteLabelDataSource
import com.noto.app.domain.label.Label
import com.noto.app.domain.label.LabelRepository
import com.noto.app.domain.settings.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class LabelRepositoryImpl(
    private val localLabelDataSource: LocalLabelDataSource,
    private val localEncryptedFolderDataSource: LocalEncryptedFolderDataSource,
    private val localEncryptedLabelDataSource: LocalEncryptedLabelDataSource,
    private val remoteLabelDataSource: RemoteLabelDataSource,
    private val remoteLabelService: RemoteLabelService,
    private val settingsRepository: SettingsRepository,
    private val labelMapper: LabelMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) : LabelRepository {

    override fun getMainLabels(): Flow<List<Label>> = localLabelDataSource.getMainLocalLabels()
        .map { it.map { labelMapper.mapLocalLabelToDomainLabel(it) } }
        .flowOn(coroutineDispatcher)

    override fun getLabelsByFolderId(folderId: Long): Flow<List<Label>> = combine(
        localLabelDataSource.getLocalLabelsByFolderId(folderId),
        localEncryptedLabelDataSource.getLocalEncryptedLabelsByFolderId(folderId)
            .map { it.map { localEncryptedLabel -> labelMapper.mapLocalEncryptedLabelToLocalLabel(localEncryptedLabel) } },
    ) { localLabels, localEncryptedLabels ->
        val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(folderId)
        if (isLocalFolderVaulted) localEncryptedLabels else localLabels
    }.map { it.map { labelMapper.mapLocalLabelToDomainLabel(it) } }
        .flowOn(coroutineDispatcher)

    override fun getLabelById(id: Long): Flow<Label> = combine(
        localLabelDataSource.getLocalLabelById(id),
        localEncryptedLabelDataSource.getLocalEncryptedLabelById(id)
            .map { it?.let { localEncryptedLabel -> labelMapper.mapLocalEncryptedLabelToLocalLabel(localEncryptedLabel) } },
    ) { localLabel, localEncryptedLabel -> localLabel ?: localEncryptedLabel }
        .filterNotNull()
        .map { labelMapper.mapLocalLabelToDomainLabel(it) }
        .flowOn(coroutineDispatcher)

    override suspend fun createLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(label.folderId)
            val position = getLabelPosition(label.folderId)
            val positionedLabel = label.copy(position = position)
            val localLabel = labelMapper.mapDomainLabelToLocalLabel(positionedLabel)
            val localLabelId = if (isLocalFolderVaulted) {
                val localEncryptedLabel = labelMapper.mapLocalLabelToLocalEncryptedLabel(localLabel)
                localEncryptedLabelDataSource.createLocalEncryptedLabel(localEncryptedLabel)
            } else {
                localLabelDataSource.createLocalLabel(localLabel)
            }

            if (settingsRepository.isUserLoggedIn.first()) remoteLabelService.createRemoteLabel(localLabel.remoteId)

            localLabelId
        }
    }

    override suspend fun updateLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(label.folderId)
            val localLabel = labelMapper.mapDomainLabelToLocalLabel(label.copy(title = label.title.trim()))
            if (isLocalFolderVaulted) {
                val localEncryptedLabel = labelMapper.mapLocalLabelToLocalEncryptedLabel(localLabel)
                localEncryptedLabelDataSource.updateLocalEncryptedLabel(localEncryptedLabel)
            } else {
                localLabelDataSource.updateLocalLabel(localLabel)
            }
            if (settingsRepository.isUserLoggedIn.first()) remoteLabelService.updateRemoteLabel(localLabel.remoteId)
        }
    }

    override suspend fun deleteLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(label.folderId)
            val localLabel = labelMapper.mapDomainLabelToLocalLabel(label)
            if (isLocalFolderVaulted) {
                val localEncryptedLabel = labelMapper.mapLocalLabelToLocalEncryptedLabel(localLabel)
                localEncryptedLabelDataSource.deleteLocalEncryptedLabel(localEncryptedLabel)
            } else {
                localLabelDataSource.deleteLocalLabel(localLabel)
            }
            if (settingsRepository.isUserLoggedIn.first()) remoteLabelService.deleteRemoteLabel(localLabel.remoteId)
        }
    }

    override suspend fun clearLabels() = withContext(coroutineDispatcher) {
        localLabelDataSource.clearLocalLabels()
        localEncryptedLabelDataSource.clearLocalEncryptedLabels()
    }

    private suspend fun getLabelPosition(folderId: Long) = withContext(coroutineDispatcher) {
        localLabelDataSource.getLocalLabelsByFolderId(folderId)
            .filterNotNull()
            .first()
            .count()
    }

}