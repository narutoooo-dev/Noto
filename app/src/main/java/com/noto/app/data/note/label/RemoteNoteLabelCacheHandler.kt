package com.noto.app.data.note.label

import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.note.label.model.RemoteNoteLabel
import com.noto.app.data.note.label.source.LocalNoteLabelDataSource
import kotlinx.coroutines.flow.first

class RemoteNoteLabelCacheHandler(
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val noteLabelMapper: NoteLabelMapper,
) : RemoteItemCacheHandler<RemoteNoteLabel> {
    override suspend fun cacheRemoteItems(remoteItems: List<RemoteNoteLabel>) {
        val remoteLocalNoteLabels = remoteItems.map { noteLabelMapper.mapRemoteNoteLabelToLocalNoteLabel(it) }
        val localNoteId = remoteLocalNoteLabels.firstOrNull()?.noteId ?: 0L
        val databaseLocalNoteLabels = localNoteLabelDataSource.getLocalNoteLabelsByNoteId(localNoteId).first()
        val remoteLocalNoteLabelIds = remoteLocalNoteLabels.map { it.labelId }
        val databaseLocalNoteLabelIds = databaseLocalNoteLabels.map { it.labelId }
        val deletedNoteLabels = databaseLocalNoteLabels.filter { it.labelId !in remoteLocalNoteLabelIds }
        val newNoteLabels = remoteLocalNoteLabels.filter { it.labelId !in databaseLocalNoteLabelIds }
        newNoteLabels.forEach { localNoteLabelDataSource.createLocalNoteLabel(it) }
        deletedNoteLabels.forEach { localNoteLabelDataSource.deleteLocalNoteLabel(it) }
    }
}