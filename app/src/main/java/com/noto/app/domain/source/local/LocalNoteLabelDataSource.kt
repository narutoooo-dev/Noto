package com.noto.app.domain.source.local

import com.noto.app.data.model.local.LocalNoteLabel
import kotlinx.coroutines.flow.Flow

interface LocalNoteLabelDataSource {

    fun getNoteLabelsByNoteId(noteId: Long): Flow<List<LocalNoteLabel>>

    fun getNoteLabels(): Flow<List<LocalNoteLabel>>

    suspend fun createNoteLabel(noteLabel: LocalNoteLabel)

    suspend fun deleteNoteLabel(noteId: Long, labelId: Long)

    suspend fun clearNoteLabels()

}