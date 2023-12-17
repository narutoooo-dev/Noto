package com.noto.app.data.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
    tableName = "labels",
    foreignKeys = [ForeignKey(
        entity = LocalFolder::class,
        parentColumns = ["id"],
        childColumns = ["folder_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LocalLabel(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @Transient
    @ColumnInfo(name = "remote_id")
    val remoteId: String = "",

    @ColumnInfo(name = "folder_id")
    val folderId: Long,

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "color")
    val color: Int = 0,

    @ColumnInfo(name = "position", defaultValue = "0")
    val position: Int = 0,
)