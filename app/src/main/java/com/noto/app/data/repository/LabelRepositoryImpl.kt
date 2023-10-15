package com.noto.app.data.repository

import com.noto.app.data.database.NotoColorConverter
import com.noto.app.data.model.local.LocalLabel
import com.noto.app.domain.model.Label
import com.noto.app.domain.repository.LabelRepository
import com.noto.app.domain.source.local.LocalLabelDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class LabelRepositoryImpl(
    private val dataSource: LocalLabelDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LabelRepository {

    override fun getAllLabels(): Flow<List<Label>> = dataSource.getAllLabels()
        .map { it.map { it.toDomainLabel() } }
        .flowOn(dispatcher)

    override fun getLabelsByFolderId(folderId: Long): Flow<List<Label>> = dataSource.getLabelsByFolderId(folderId)
        .map { it.map { it.toDomainLabel() } }
        .flowOn(dispatcher)

    override fun getLabelById(id: Long): Flow<Label> = dataSource.getLabelById(id)
        .filterNotNull()
        .map { it.toDomainLabel() }
        .flowOn(dispatcher)

    override suspend fun createLabel(label: Label, overridePosition: Boolean) = withContext(dispatcher) {
        val position = if (overridePosition) getLabelPosition(label.folderId) else label.position
        dataSource.createLabel(label.copy(position = position).toLocalLabel())
    }

    override suspend fun updateLabel(label: Label) = withContext(dispatcher) {
        dataSource.updateLabel(label.copy(title = label.title.trim()).toLocalLabel())
    }

    override suspend fun deleteLabel(label: Label) = withContext(dispatcher) {
        dataSource.deleteLabel(label.toLocalLabel())
    }

    override suspend fun clearLabels() = withContext(dispatcher) {
        dataSource.clearLabels()
    }

    private suspend fun getLabelPosition(folderId: Long) = withContext(dispatcher) {
        dataSource.getLabelsByFolderId(folderId)
            .filterNotNull()
            .first()
            .count()
    }

    private fun LocalLabel.toDomainLabel(): Label {
        return Label(
            id,
            folderId,
            title,
            NotoColorConverter.toEnum(color),
            position,
        )
    }

    private fun Label.toLocalLabel(): LocalLabel {
        return LocalLabel(
            id,
            folderId,
            title,
            NotoColorConverter.toOrdinal(color),
            position,
        )
    }

}