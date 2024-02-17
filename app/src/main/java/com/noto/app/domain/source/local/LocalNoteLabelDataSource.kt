package com.noto.app.domain.source.local

import com.noto.app.data.model.local.LocalNoteLabel
import kotlinx.coroutines.flow.Flow

interface LocalNoteLabelDataSource {

    fun getAllLocalNoteLabels(): Flow<List<LocalNoteLabel>>

    fun getLocalNoteLabelsByNoteId(localNoteId: Long): Flow<List<LocalNoteLabel>>

    fun getLocalNoteLabelByRemoteId(remoteNoteLabelId: String): Flow<LocalNoteLabel?>

    suspend fun createLocalNoteLabel(localNoteLabel: LocalNoteLabel)

    suspend fun deleteLocalNoteLabel(localNoteLabel: LocalNoteLabel)

    suspend fun clearLocalNoteLabels()

}