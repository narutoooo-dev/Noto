package com.noto.app.data.folder.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encrypted_folders")
data class LocalEncryptedFolder(
    @PrimaryKey
    @ColumnInfo("id")
    val id: Long,
    @ColumnInfo(name = "remote_id")
    val remoteId: String,
    @ColumnInfo(name = "keyset", defaultValue = "NULL")
    val keyset: String?,
    @ColumnInfo(name = "parent_id", defaultValue = "NULL")
    val parentId: Long?,
    @ColumnInfo("content")
    val content: String,
)