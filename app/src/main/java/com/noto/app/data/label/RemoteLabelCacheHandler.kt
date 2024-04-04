package com.noto.app.data.label

import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.label.model.RemoteLabel
import com.noto.app.data.label.source.LocalLabelDataSource
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