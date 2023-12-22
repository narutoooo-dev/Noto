package com.noto.app.data.worker.folder

import com.noto.app.data.model.mapper.FolderMapper
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import org.koin.core.component.get

sealed interface RemoteFolderWorker : RemoteItemWorker {

    val localFolderDataSource get() = get<LocalFolderDataSource>()

    val remoteFolderDataSource get() = get<RemoteFolderDataSource>()

    val folderMapper get() = get<FolderMapper>()

}