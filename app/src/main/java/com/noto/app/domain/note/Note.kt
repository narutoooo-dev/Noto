package com.noto.app.domain.note

import com.noto.app.domain.folder.Folder
import com.noto.app.domain.label.Label
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Note(
    val id: Long,
    val folderId: Long,
    val title: String,
    val body: String,
    val position: Int,
    val creationDate: Instant,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val reminderDate: Instant?,
    val isVaulted: Boolean,
    val accessDate: Instant,
    val scrollingPosition: Int,
    val labels: List<Label>,
) {
    companion object {
        val Default = Note(
            id = 0L,
            folderId = Folder.GeneralFolderId,
            title = String(),
            body = String(),
            position = 0,
            creationDate = Clock.System.now(),
            isPinned = false,
            isArchived = false,
            reminderDate = null,
            isVaulted = false,
            accessDate = Clock.System.now(),
            scrollingPosition = 0,
            labels = emptyList(),
        )
    }
}