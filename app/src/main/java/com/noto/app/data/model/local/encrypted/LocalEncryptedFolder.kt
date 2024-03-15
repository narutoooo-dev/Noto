package com.noto.app.data.model.local.encrypted

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encrypted_folders")
data class LocalEncryptedFolder(
    @PrimaryKey
    @ColumnInfo("id")
    val id: Long,
    @ColumnInfo(name = "parent_id", defaultValue = "NULL")
    val parentId: Long?,
    @ColumnInfo("content")
    val content: String,
)