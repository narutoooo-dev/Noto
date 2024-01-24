package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteNote
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupabaseNoteClient(private val client: SupabaseClient) : RemoteNoteDataSource {

    override suspend fun getAllRemoteNotes(): List<RemoteNote> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Notes]
                .select()
                .decodeList()
        }
    }

    override suspend fun getRemoteNotesByFolderId(remoteFolderId: String): List<RemoteNote> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Notes]
                .select {
                    filter {
                        RemoteNote::folderId eq remoteFolderId
                    }
                }
                .decodeList()
        }
    }

    override suspend fun createRemoteNote(remoteNote: RemoteNote) {
        client.postgrest[SupabaseConstants.Tables.Notes]
            .insert(remoteNote)
    }

    override suspend fun updateRemoteNote(remoteNote: RemoteNote) {
        client.postgrest[SupabaseConstants.Tables.Notes]
            .update(remoteNote) {
                filter {
                    RemoteNote::id eq remoteNote.id
                }
            }
    }

    override suspend fun deleteRemoteNoteById(remoteNoteId: String) {
        client.postgrest[SupabaseConstants.Tables.Notes]
            .delete {
                filter {
                    RemoteNote::id eq remoteNoteId
                }
            }
    }

}