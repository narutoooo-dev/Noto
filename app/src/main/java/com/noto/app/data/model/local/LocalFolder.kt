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
    @ColumnInfo(name = "encrypted_key", defaultValue = "NULL")
    val encryptedKey: ByteArray? = null,

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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalFolder

        if (id != other.id) return false
        if (remoteId != other.remoteId) return false
        if (encryptedKey != null) {
            if (other.encryptedKey == null) return false
            if (!encryptedKey.contentEquals(other.encryptedKey)) return false
        } else if (other.encryptedKey != null) return false
        if (parentId != other.parentId) return false
        if (title != other.title) return false
        if (position != other.position) return false
        if (color != other.color) return false
        if (creationDate != other.creationDate) return false
        if (layout != other.layout) return false
        if (notePreviewSize != other.notePreviewSize) return false
        if (isArchived != other.isArchived) return false
        if (isPinned != other.isPinned) return false
        if (isShowNoteCreationDate != other.isShowNoteCreationDate) return false
        if (newNoteCursorPosition != other.newNoteCursorPosition) return false
        if (sortingType != other.sortingType) return false
        if (sortingOrder != other.sortingOrder) return false
        if (grouping != other.grouping) return false
        if (groupingOrder != other.groupingOrder) return false
        if (isVaulted != other.isVaulted) return false
        if (scrollingPosition != other.scrollingPosition) return false
        if (filteringType != other.filteringType) return false
        if (openNotesIn != other.openNotesIn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + remoteId.hashCode()
        result = 31 * result + (encryptedKey?.contentHashCode() ?: 0)
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + title.hashCode()
        result = 31 * result + position
        result = 31 * result + color
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + layout
        result = 31 * result + notePreviewSize
        result = 31 * result + isArchived.hashCode()
        result = 31 * result + isPinned.hashCode()
        result = 31 * result + isShowNoteCreationDate.hashCode()
        result = 31 * result + newNoteCursorPosition
        result = 31 * result + sortingType
        result = 31 * result + sortingOrder
        result = 31 * result + grouping
        result = 31 * result + groupingOrder
        result = 31 * result + isVaulted.hashCode()
        result = 31 * result + scrollingPosition
        result = 31 * result + filteringType
        result = 31 * result + openNotesIn
        return result
    }
}