package com.noto.app.domain.label

import kotlinx.coroutines.flow.Flow

interface LabelRepository {

    fun getMainLabels(): Flow<List<Label>>

    fun getLabelsByFolderId(folderId: Long): Flow<List<Label>>

    fun getLabelById(id: Long): Flow<Label>

    suspend fun createLabel(label: Label): Result<Long>

    suspend fun updateLabel(label: Label): Result<Unit>

    suspend fun deleteLabel(label: Label): Result<Unit>

    suspend fun clearLabels()

}