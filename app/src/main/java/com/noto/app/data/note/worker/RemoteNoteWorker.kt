package com.noto.app.data.note.worker

import com.noto.app.data.RemoteItemWorker
import com.noto.app.data.note.NoteMapper
import com.noto.app.data.note.label.NoteLabelMapper
import com.noto.app.data.note.label.source.LocalNoteLabelDataSource
import com.noto.app.data.note.label.source.RemoteNoteLabelDataSource
import com.noto.app.data.note.source.LocalNoteDataSource
import com.noto.app.data.note.source.RemoteNoteDataSource
import org.koin.core.component.get

sealed interface RemoteNoteWorker : RemoteItemWorker {

    val localNoteDataSource get() = get<LocalNoteDataSource>()

    val remoteNoteDataSource get() = get<RemoteNoteDataSource>()

    val localNoteLabelDataSource get() = get<LocalNoteLabelDataSource>()

    val remoteNoteLabelDataSource get() = get<RemoteNoteLabelDataSource>()

    val noteMapper get() = get<NoteMapper>()

    val noteLabelMapper get() = get<NoteLabelMapper>()

}