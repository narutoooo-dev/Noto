package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteNoteLabel

interface RemoteNoteLabelDataSource {

    suspend fun getAllRemoteNoteLabels(): List<RemoteNoteLabel>

    suspend fun getRemoteNoteLabelsByNoteId(remoteNoteId: String): List<RemoteNoteLabel>

    suspend fun createRemoteNoteLabel(remoteNoteLabel: RemoteNoteLabel)

    suspend fun updateRemoteNoteLabel(remoteNoteLabel: RemoteNoteLabel)

    suspend fun deleteNoteLabelBy(remoteNoteLabelId: String)

}