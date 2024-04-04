package com.noto.app.data.label.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.noto.app.data.folder.model.LocalEncryptedFolder

@Entity(
    tableName = "encrypted_labels",
    foreignKeys = [
        ForeignKey(
            entity = LocalEncryptedFolder::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LocalEncryptedLabel(
    @PrimaryKey
    @ColumnInfo("id")
    val id: Long,
    @ColumnInfo(name = "remote_id")
    val remoteId: String,
    @ColumnInfo(name = "folder_id")
    val folderId: Long,
    @ColumnInfo("content")
    val content: String,
)