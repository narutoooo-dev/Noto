package com.noto.app.data.label

import com.noto.app.data.label.source.LocalLabelDataSource
import com.noto.app.data.label.source.RemoteLabelDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LabelSyncService(
    private val remoteLabelDataSource: RemoteLabelDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val labelMapper: LabelMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) {

    suspend fun runManualLabelSyncService(timestamp: String) = runCatching {
        withContext(coroutineDispatcher) {
            remoteLabelDataSource.getRemoteLabelsSince(timestamp)
                .map { labelMapper.mapRemoteLabelToLocalLabel(it) }
                .forEach { localLabelDataSource.upsertLocalLabel(it) }
        }
    }

    suspend fun startLabelSyncService() {
        coroutineScope {
            withContext(coroutineDispatcher) {
                launch { initCreateLabelListener() }
                launch { initUpdateLabelListener() }
                launch { initDeleteLabelListener() }
                launch { remoteLabelDataSource.subscribeToRemoteLabelListeners() }
            }
        }
    }

    suspend fun stopLabelSyncService() {
        withContext(coroutineDispatcher) {
            remoteLabelDataSource.unsubscribeToRemoteLabelListeners()
        }
    }

    private suspend fun initCreateLabelListener() {
        withContext(coroutineDispatcher) {
            remoteLabelDataSource.createRemoteLabelListener()
                .map(labelMapper::mapRemoteLabelToLocalLabel)
                .filter { localLabelDataSource.getLocalLabelByRemoteId(it.remoteId).first() == null }
                .collect(localLabelDataSource::createLocalLabel)
        }
    }

    private suspend fun initUpdateLabelListener() {
        withContext(coroutineDispatcher) {
            remoteLabelDataSource.updateRemoteLabelListener()
                .map(labelMapper::mapRemoteLabelToLocalLabel)
                .map {
                    val localId = localLabelDataSource.getLocalLabelByRemoteId(it.remoteId).first()?.id ?: 0L
                    it.copy(id = localId)
                }
                .collect(localLabelDataSource::updateLocalLabel)
        }
    }

    private suspend fun initDeleteLabelListener() {
        withContext(coroutineDispatcher) {
            remoteLabelDataSource.deleteRemoteLabelListener()
                .collect(localLabelDataSource::deleteLocalLabelByRemoteId)
        }
    }

}