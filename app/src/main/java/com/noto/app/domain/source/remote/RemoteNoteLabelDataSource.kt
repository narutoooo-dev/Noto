package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteNoteLabel
import kotlinx.coroutines.flow.Flow

interface RemoteNoteLabelDataSource {

    suspend fun getAllRemoteNoteLabels(): List<RemoteNoteLabel>

    suspend fun getRemoteNoteLabelsByNoteId(remoteNoteId: String): List<RemoteNoteLabel>

    suspend fun createRemoteNoteLabel(remoteNoteLabel: RemoteNoteLabel)

    suspend fun deleteRemoteNoteLabelById(remoteNoteLabelId: String)

    suspend fun subscribeToRemoteNoteLabelListeners()

    suspend fun unsubscribeToRemoteNoteLabelListeners()

    suspend fun createRemoteNoteLabelListener(): Flow<RemoteNoteLabel>

    suspend fun deleteRemoteNoteLabelListener(): Flow<String>

}