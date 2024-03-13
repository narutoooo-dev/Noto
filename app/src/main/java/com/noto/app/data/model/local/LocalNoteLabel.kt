package com.noto.app.data.model.local

import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
    tableName = "note_labels",
    foreignKeys = [
        ForeignKey(
            entity = LocalNote::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LocalLabel::class,
            parentColumns = ["id"],
            childColumns = ["label_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("remote_id", name = "index_note_labels_remote_id", unique = true)]
)
data class LocalNoteLabel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @Transient
    @ColumnInfo(name = "remote_id")
    val remoteId: String = "",

    @ColumnInfo(name = "note_id")
    val noteId: Long,

    @ColumnInfo(name = "label_id")
    val labelId: Long
)