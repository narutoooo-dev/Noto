package com.noto.app.data.cache

import com.noto.app.data.model.mapper.LabelMapper
import com.noto.app.data.model.remote.RemoteLabel
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalLabelDataSource
import kotlinx.coroutines.flow.first

class RemoteLabelCacheHandler(
    private val localLabelDataSource: LocalLabelDataSource,
    private val labelMapper: LabelMapper,
) : RemoteItemCacheHandler<RemoteLabel> {
    override suspend fun cacheRemoteItems(remoteItems: List<RemoteLabel>) {
        remoteItems.forEach { remoteLabel ->
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