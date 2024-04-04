package com.noto.app.data.label.worker

import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.label.LabelMapper
import com.noto.app.data.label.source.LocalLabelDataSource
import com.noto.app.data.label.source.RemoteLabelDataSource
import org.koin.core.component.get

sealed interface RemoteLabelWorker : RemoteItemWorker {

    val localLabelDataSource get() = get<LocalLabelDataSource>()

    val remoteLabelDataSource get() = get<RemoteLabelDataSource>()

    val labelMapper get() = get<LabelMapper>()

}