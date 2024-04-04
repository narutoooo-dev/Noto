package com.noto.app.data.note.label.source

import com.noto.app.data.note.label.model.RemoteNoteLabel
import kotlinx.coroutines.flow.Flow

interface RemoteNoteLabelDataSource {

    suspend fun getAllRemoteNoteLabels(): List<RemoteNoteLabel>

    suspend fun getRemoteNoteLabelsSince(timestamp: String): List<RemoteNoteLabel>

    suspend fun getRemoteNoteLabelsByNoteId(remoteNoteId: String): List<RemoteNoteLabel>

    suspend fun createRemoteNoteLabel(remoteNoteLabel: RemoteNoteLabel)

    suspend fun deleteRemoteNoteLabelById(remoteNoteLabelId: String)

    suspend fun subscribeToRemoteNoteLabelListeners()

    suspend fun unsubscribeToRemoteNoteLabelListeners()

    suspend fun createRemoteNoteLabelListener(): Flow<RemoteNoteLabel>

    suspend fun deleteRemoteNoteLabelListener(): Flow<String>

}