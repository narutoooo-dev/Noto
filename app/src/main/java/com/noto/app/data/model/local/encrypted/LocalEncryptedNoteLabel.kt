package com.noto.app.data.model.local.encrypted

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "encrypted_note_labels",
    foreignKeys = [
        ForeignKey(
            entity = LocalEncryptedNote::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LocalEncryptedLabel::class,
            parentColumns = ["id"],
            childColumns = ["label_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LocalEncryptedNoteLabel(
    @PrimaryKey
    @ColumnInfo("id")
    val id: Long,
    @ColumnInfo(name = "remote_id")
    val remoteId: String,
    @ColumnInfo(name = "note_id")
    val noteId: Long,
    @ColumnInfo(name = "label_id")
    val labelId: Long,
)