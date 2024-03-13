package com.noto.app.data.model.local

import androidx.room.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = LocalFolder::class,
        parentColumns = ["id"],
        childColumns = ["folder_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("remote_id", name = "index_labels_remote_id", unique = true)]
)
data class LocalNote(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @Transient
    @ColumnInfo(name = "remote_id")
    val remoteId: String = "",

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
