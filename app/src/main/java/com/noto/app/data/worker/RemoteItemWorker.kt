package com.noto.app.data.worker

import com.noto.app.util.KoinModules
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface RemoteItemWorker : KoinComponent {

    val coroutineDispatcher get() = get<CoroutineDispatcher>(KoinModules.Qualifiers.CoroutineDispatcher)

    companion object {
        const val NewItemId = 0L
        const val GeneralFolderId = -1L
        const val RemoteFolderId = "remote_folder_id"
        const val RemoteNoteId = "remote_note_id"
        const val RemoteLabelId = "remote_label_id"
        const val OldRemoteNoteLabelIds = "old_remote_note_label_ids"
        const val NewRemoteNoteLabelIds = "new_remote_note_label_ids"
    }

}