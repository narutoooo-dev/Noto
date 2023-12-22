package com.noto.app.data.repository

import com.noto.app.data.model.mapper.LabelMapper
import com.noto.app.domain.model.Label
import com.noto.app.domain.repository.LabelRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.service.RemoteLabelService
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.remote.RemoteLabelDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class LabelRepositoryImpl(
    private val localLabelDataSource: LocalLabelDataSource,
    private val remoteLabelDataSource: RemoteLabelDataSource,
    private val remoteLabelService: RemoteLabelService,
    private val settingsRepository: SettingsRepository,
    private val labelMapper: LabelMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) : LabelRepository {

    override fun getMainLabels(): Flow<List<Label>> = localLabelDataSource.getMainLocalLabels()
        .map { it.map { labelMapper.mapLocalLabelToDomainLabel(it) } }
        .flowOn(coroutineDispatcher)

    override fun getLabelsByFolderId(folderId: Long): Flow<List<Label>> = localLabelDataSource.getLocalLabelsByFolderId(folderId)
        .map { it.map { labelMapper.mapLocalLabelToDomainLabel(it) } }
        .flowOn(coroutineDispatcher)

    override fun getLabelById(id: Long): Flow<Label> = localLabelDataSource.getLocalLabelById(id)
        .filterNotNull()
        .map { labelMapper.mapLocalLabelToDomainLabel(it) }
        .flowOn(coroutineDispatcher)

    override suspend fun createLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val position = getLabelPosition(label.folderId)
            val positionedLabel = label.copy(position = position)
            val localLabel = labelMapper.mapDomainLabelToLocalLabel(positionedLabel)
            val localLabelId = localLabelDataSource.createLocalLabel(localLabel)

            if (settingsRepository.isUserLoggedIn.first()) remoteLabelService.createRemoteLabel(localLabel.remoteId)

            localLabelId
        }
    }

    override suspend fun updateLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val localLabel = labelMapper.mapDomainLabelToLocalLabel(label.copy(title = label.title.trim()))
            localLabelDataSource.updateLocalLabel(localLabel)
            if (settingsRepository.isUserLoggedIn.first()) remoteLabelService.updateRemoteLabel(localLabel.remoteId)
        }
    }

    override suspend fun deleteLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val localLabel = labelMapper.mapDomainLabelToLocalLabel(label)
            localLabelDataSource.deleteLocalLabel(localLabel)
            if (settingsRepository.isUserLoggedIn.first()) remoteLabelService.deleteRemoteLabel(localLabel.remoteId)
        }
    }

    override suspend fun clearLabels() = withContext(coroutineDispatcher) {
        localLabelDataSource.clearLabels()
    }

    private suspend fun getLabelPosition(folderId: Long) = withContext(coroutineDispatcher) {
        localLabelDataSource.getLocalLabelsByFolderId(folderId)
            .filterNotNull()
            .first()
            .count()
    }

}