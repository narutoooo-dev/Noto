package com.noto.app.data.fetcher

import com.noto.app.data.model.mapper.LabelMapper
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.remote.RemoteLabelDataSource
import kotlinx.coroutines.flow.first

class RemoteLabelsFetcher(
    private val remoteLabelDataSource: RemoteLabelDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val labelMapper: LabelMapper,
) {
    suspend fun fetchRemoteLabels(remoteFolderId: String?) {
        val remoteLabels = if (remoteFolderId == null) {
            remoteLabelDataSource.getAllRemoteLabels()
        } else {
            remoteLabelDataSource.getRemoteLabelsByFolderId(remoteFolderId)
        }
        remoteLabels.forEach { remoteLabel ->
            val databaseLocalLabel = localLabelDataSource.getLocalLabelByRemoteId(remoteLabel.id.toString()).first()
            val remoteLocalLabel = labelMapper.mapRemoteLabelToLocalLabel(remoteLabel)
            if (databaseLocalLabel == null) {
                localLabelDataSource.createLocalLabel(remoteLocalLabel.copy(id = RemoteItemWorker.NewItemId))
            } else {
                localLabelDataSource.updateLocalLabel(remoteLocalLabel.copy(id = databaseLocalLabel.id))
            }
        }
    }
}