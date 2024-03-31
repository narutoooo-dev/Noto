package com.noto.app.data.model.local.encrypted

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "encrypted_notes",
    foreignKeys = [
        ForeignKey(
            entity = LocalEncryptedFolder::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LocalEncryptedNote(
    @PrimaryKey
    @ColumnInfo("id")
    val id: Long,
    @ColumnInfo(name = "remote_id")
    val remoteId: String,
    @ColumnInfo(name = "folder_id")
    val folderId: Long,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,
    @ColumnInfo("content")
    val content: String,
)