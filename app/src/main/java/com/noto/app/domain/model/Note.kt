package com.noto.app.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Note(
    val id: Long = 0L,
    val folderId: Long,
    val title: String = String(),
    val body: String = String(),
    val position: Int,
    val creationDate: Instant = Clock.System.now(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val reminderDate: Instant? = null,
    val isVaulted: Boolean = false,
    val accessDate: Instant = creationDate,
    val scrollingPosition: Int = 0,
) {
    companion object
}