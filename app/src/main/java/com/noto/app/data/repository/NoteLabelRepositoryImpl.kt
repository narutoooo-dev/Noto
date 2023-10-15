package com.noto.app.data.repository

import com.noto.app.data.model.local.LocalNoteLabel
import com.noto.app.domain.model.NoteLabel
import com.noto.app.domain.repository.NoteLabelRepository
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NoteLabelRepositoryImpl(
    private val source: LocalNoteLabelDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : NoteLabelRepository {

    override fun getNoteLabelsByNoteId(noteId: Long): Flow<List<NoteLabel>> = source.getNoteLabelsByNoteId(noteId)
        .map { it.map { it.toDomainNoteLabel() } }
        .flowOn(dispatcher)

    override fun getNoteLabels(): Flow<List<NoteLabel>> = source.getNoteLabels()
        .map { it.map { it.toDomainNoteLabel() } }
        .flowOn(dispatcher)

    override suspend fun createNoteLabel(noteLabel: NoteLabel) = withContext(dispatcher) {
        source.createNoteLabel(noteLabel.toLocalNoteLabel())
    }

    override suspend fun deleteNoteLabel(noteId: Long, labelId: Long) = withContext(dispatcher) {
        source.deleteNoteLabel(noteId, labelId)
    }

    override suspend fun clearNoteLabels() = withContext(dispatcher) {
        source.clearNoteLabels()
    }

    private fun LocalNoteLabel.toDomainNoteLabel(): NoteLabel {
        return NoteLabel(id, noteId, labelId)
    }

    private fun NoteLabel.toLocalNoteLabel(): LocalNoteLabel {
        return LocalNoteLabel(id, noteId, labelId)
    }

}