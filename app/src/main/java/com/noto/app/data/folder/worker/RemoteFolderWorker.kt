package com.noto.app.data.folder.worker

import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.folder.FolderMapper
import com.noto.app.data.folder.source.LocalFolderDataSource
import com.noto.app.data.folder.source.RemoteFolderDataSource
import org.koin.core.component.get

sealed interface RemoteFolderWorker : RemoteItemWorker {

    val localFolderDataSource get() = get<LocalFolderDataSource>()

    val remoteFolderDataSource get() = get<RemoteFolderDataSource>()

    val folderMapper get() = get<FolderMapper>()

}