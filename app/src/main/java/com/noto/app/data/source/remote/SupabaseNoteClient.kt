package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteNote
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.jsonPrimitive

class SupabaseNoteClient(private val client: SupabaseClient) : RemoteNoteDataSource {

    private val insertNoteChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Note.Insert) }
    private val updateNoteChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Note.Update) }
    private val deleteNoteChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Note.Delete) }

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

    override suspend fun subscribeToRemoteNoteListeners() {
        client.realtime.connect()
        insertNoteChannel.subscribe()
        updateNoteChannel.subscribe()
        deleteNoteChannel.subscribe()
    }

    override suspend fun unsubscribeToRemoteNoteListeners() {
        insertNoteChannel.unsubscribe()
        updateNoteChannel.unsubscribe()
        deleteNoteChannel.unsubscribe()
        client.realtime.disconnect()
    }

    override suspend fun createRemoteNoteListener(): Flow<RemoteNote> {
        return insertNoteChannel.postgresChangeFlow<PostgresAction.Insert>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Notes
        }.map { it.decodeRecord<RemoteNote>() }
    }

    override suspend fun updateRemoteNoteListener(): Flow<RemoteNote> {
        return updateNoteChannel.postgresChangeFlow<PostgresAction.Update>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Notes
        }.map { it.decodeRecord<RemoteNote>() }
    }

    override suspend fun deleteRemoteNoteListener(): Flow<String> {
        return deleteNoteChannel.postgresChangeFlow<PostgresAction.Delete>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Notes
        }.map { it.oldRecord.getValue(SupabaseConstants.Id).jsonPrimitive.content }
    }

}