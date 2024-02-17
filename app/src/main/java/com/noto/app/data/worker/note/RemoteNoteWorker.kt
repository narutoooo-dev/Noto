package com.noto.app.data.worker.note

import com.noto.app.data.model.mapper.NoteLabelMapper
import com.noto.app.data.model.mapper.NoteMapper
import com.noto.app.data.worker.RemoteItemWorker
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import com.noto.app.domain.source.remote.RemoteNoteLabelDataSource
import org.koin.core.component.get

sealed interface RemoteNoteWorker : RemoteItemWorker {

    val localNoteDataSource get() = get<LocalNoteDataSource>()

    val remoteNoteDataSource get() = get<RemoteNoteDataSource>()

    val localNoteLabelDataSource get() = get<LocalNoteLabelDataSource>()

    val remoteNoteLabelDataSource get() = get<RemoteNoteLabelDataSource>()

    val noteMapper get() = get<NoteMapper>()

    val noteLabelMapper get() = get<NoteLabelMapper>()

}