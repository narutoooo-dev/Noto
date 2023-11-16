package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteNote
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning

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
                .select { RemoteNote::folderId eq remoteFolderId }
                .decodeList()
        }
    }

    override suspend fun createRemoteNote(remoteNote: RemoteNote) {
        client.postgrest[SupabaseConstants.Tables.Notes]
            .insert(remoteNote, returning = Returning.MINIMAL)
    }

    override suspend fun updateRemoteNote(remoteNote: RemoteNote) {
        client.postgrest[SupabaseConstants.Tables.Notes]
            .update(remoteNote, returning = Returning.MINIMAL) { RemoteNote::id eq remoteNote.id }
    }

    override suspend fun deleteRemoteNoteById(remoteNoteId: String) {
        client.postgrest[SupabaseConstants.Tables.Notes]
            .delete(returning = Returning.MINIMAL) { RemoteNote::id eq remoteNoteId }
    }

}