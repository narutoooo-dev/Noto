package com.noto.app.data.repository

import com.noto.app.data.model.DomainMappers
import com.noto.app.data.model.LocalMappers
import com.noto.app.data.model.local.LocalLabel
import com.noto.app.domain.model.Label
import com.noto.app.domain.repository.LabelRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.service.RemoteLabelService
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.remote.RemoteLabelDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID

class LabelRepositoryImpl(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val remoteLabelDataSource: RemoteLabelDataSource,
    private val remoteLabelService: RemoteLabelService,
    private val settingsRepository: SettingsRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) : LabelRepository {

    override fun getMainLabels(): Flow<List<Label>> = localLabelDataSource.getMainLocalLabels()
        .map { it.map { it.toDomainLabel() } }
        .flowOn(coroutineDispatcher)

    override fun getLabelsByFolderId(folderId: Long): Flow<List<Label>> = localLabelDataSource.getLocalLabelsByFolderId(folderId)
        .map {
            if (settingsRepository.isUserLoggedIn.first()) {
                val remoteFolderId = localFolderDataSource.getLocalFolderById(folderId).firstOrNull()?.remoteId
                if (remoteFolderId != null) remoteLabelService.getRemoteLabelsByFolderId(remoteFolderId)
            }
            it.map { it.toDomainLabel() }
        }
        .flowOn(coroutineDispatcher)

    override fun getLabelById(id: Long): Flow<Label> = localLabelDataSource.getLocalLabelById(id)
        .filterNotNull()
        .map { it.toDomainLabel() }
        .flowOn(coroutineDispatcher)

    override suspend fun createLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val position = getLabelPosition(label.folderId)
            val positionedLabel = label.copy(position = position)
            val localLabel = positionedLabel.toLocalLabel()
            val localLabelId = localLabelDataSource.createLocalLabel(localLabel)

            if (settingsRepository.isUserLoggedIn.first()) remoteLabelService.createRemoteLabel(localLabel.remoteId)

            localLabelId
        }
    }

    override suspend fun updateLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val localLabel = label.copy(title = label.title.trim()).toLocalLabel()
            localLabelDataSource.updateLocalLabel(localLabel)
            if (settingsRepository.isUserLoggedIn.first()) remoteLabelService.updateRemoteLabel(localLabel.remoteId)
        }
    }

    override suspend fun deleteLabel(label: Label) = runCatching {
        withContext(coroutineDispatcher) {
            val localLabel = label.toLocalLabel()
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

    private fun LocalLabel.toDomainLabel(): Label {
        return Label(
            id = id,
            folderId = folderId,
            title = title,
            color = LocalMappers.NotoColor.map(color),
            position = position,
        )
    }

    private suspend fun Label.toLocalLabel(): LocalLabel {
        val localLabel = localLabelDataSource.getLocalLabelById(id).firstOrNull()
        val remoteId = localLabel?.remoteId ?: UUID.randomUUID().toString()
        return LocalLabel(
            id = id,
            remoteId = remoteId,
            folderId = folderId,
            title = title,
            color = DomainMappers.NotoColor.map(color),
            position = position,
        )
    }

}