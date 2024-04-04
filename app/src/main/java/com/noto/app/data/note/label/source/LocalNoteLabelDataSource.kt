package com.noto.app.data.note.label.source

import com.noto.app.data.note.label.model.LocalNoteLabel
import kotlinx.coroutines.flow.Flow

interface LocalNoteLabelDataSource {

    fun getAllLocalNoteLabels(): Flow<List<LocalNoteLabel>>

    fun getLocalNoteLabelsByNoteId(localNoteId: Long): Flow<List<LocalNoteLabel>>

    fun getLocalNoteLabelByRemoteId(remoteNoteLabelId: String): Flow<LocalNoteLabel?>

    suspend fun createLocalNoteLabel(localNoteLabel: LocalNoteLabel)

    suspend fun deleteLocalNoteLabel(localNoteLabel: LocalNoteLabel)

    suspend fun deleteLocalNoteLabelByRemoteId(remoteNoteLabelId: String)

    suspend fun clearLocalNoteLabels()

}