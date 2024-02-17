package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteNoteLabel
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteNoteLabelDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupabaseNoteLabelClient(private val client: SupabaseClient) : RemoteNoteLabelDataSource {

    override suspend fun getAllRemoteNoteLabels(): List<RemoteNoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.NoteLabels]
                .select()
                .decodeList()
        }
    }

    override suspend fun getRemoteNoteLabelsByNoteId(remoteNoteId: String): List<RemoteNoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.NoteLabels]
                .select {
                    filter {
                        RemoteNoteLabel::noteId eq remoteNoteId
                    }
                }
                .decodeList()
        }
    }

    override suspend fun createRemoteNoteLabel(remoteNoteLabel: RemoteNoteLabel) {
        client.postgrest[SupabaseConstants.Tables.NoteLabels]
            .insert(remoteNoteLabel)
    }

    override suspend fun deleteRemoteNoteLabelById(remoteNoteLabelId: String) {
        client.postgrest[SupabaseConstants.Tables.NoteLabels]
            .delete {
                filter {
                    RemoteNoteLabel::id eq remoteNoteLabelId
                }
            }
    }

}