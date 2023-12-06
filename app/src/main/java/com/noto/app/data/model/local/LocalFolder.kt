package com.noto.app.data.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "folders")
data class LocalFolder(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @Transient
    @ColumnInfo(name = "remote_id")
    val remoteId: String = "",

    @Transient
    @ColumnInfo(name = "keyset", defaultValue = "NULL")
    val keyset: String? = null,

    @ColumnInfo(name = "parent_id", defaultValue = "NULL")
    val parentId: Long?,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "color")
    val color: Int,

    @ColumnInfo(name = "creation_date")
    val creationDate: String,

    @ColumnInfo(name = "layout", defaultValue = "0")
    val layout: Int,

    @ColumnInfo(name = "note_preview_size", defaultValue = "15")
    val notePreviewSize: Int,

    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean,

    @ColumnInfo(name = "is_pinned", defaultValue = "0")
    val isPinned: Boolean,

    @ColumnInfo(name = "is_show_note_creation_date", defaultValue = "0")
    val isShowNoteCreationDate: Boolean,

    @ColumnInfo(name = "new_note_cursor_position", defaultValue = "0")
    val newNoteCursorPosition: Int,

    @ColumnInfo(name = "sorting_type", defaultValue = "1")
    val sortingType: Int,

    @ColumnInfo(name = "sorting_order", defaultValue = "1")
    val sortingOrder: Int,

    @ColumnInfo(name = "grouping", defaultValue = "0")
    val grouping: Int,

    @ColumnInfo(name = "grouping_order", defaultValue = "1")
    val groupingOrder: Int,

    @ColumnInfo(name = "is_vaulted", defaultValue = "0")
    val isVaulted: Boolean,

    @ColumnInfo(name = "scrolling_position", defaultValue = "0")
    val scrollingPosition: Int,

    @ColumnInfo(name = "filtering_type", defaultValue = "0")
    val filteringType: Int,

    @ColumnInfo(name = "open_notes_in", defaultValue = "0")
    val openNotesIn: Int,
)