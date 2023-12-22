package com.noto.app.data.worker.label

import com.noto.app.data.model.mapper.LabelMapper
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.remote.RemoteLabelDataSource
import org.koin.core.component.get

sealed interface RemoteLabelWorker : RemoteItemWorker {

    val localLabelDataSource get() = get<LocalLabelDataSource>()

    val remoteLabelDataSource get() = get<RemoteLabelDataSource>()

    val labelMapper get() = get<LabelMapper>()

}