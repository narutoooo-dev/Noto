package com.noto.app.data.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = LocalFolder::class,
        parentColumns = ["id"],
        childColumns = ["folder_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LocalNote(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "folder_id")
    val folderId: Long,

    @ColumnInfo(name = "title")
    val title: String = String(),

    @ColumnInfo(name = "body")
    val body: String = String(),

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "creation_date")
    val creationDate: String = Clock.System.now().toString(),

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "reminder_date")
    val reminderDate: String? = null,

    @ColumnInfo(name = "is_vaulted", defaultValue = "0")
    val isVaulted: Boolean = false,

    @ColumnInfo(name = "access_date", defaultValue = "creation_date")
    val accessDate: String = creationDate,

    @ColumnInfo(name = "scrolling_position", defaultValue = "0")
    val scrollingPosition: Int = 0,
)
