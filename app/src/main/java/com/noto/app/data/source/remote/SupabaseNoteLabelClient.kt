package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteNoteLabel
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteNoteLabelDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.jsonPrimitive

class SupabaseNoteLabelClient(private val client: SupabaseClient) : RemoteNoteLabelDataSource {

    private val insertNoteLabelChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.NoteLabel.Insert) }
    private val deleteNoteLabelChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.NoteLabel.Delete) }

    override suspend fun getAllRemoteNoteLabels(): List<RemoteNoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.NoteLabels]
                .select()
                .decodeList()
        }
    }

    override suspend fun getRemoteNoteLabelsSince(timestamp: String): List<RemoteNoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.NoteLabels]
                .select {
                    filter {
                        gt(SupabaseConstants.MetaDataCreatedAtColumn, timestamp)
                    }
                }
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

    override suspend fun subscribeToRemoteNoteLabelListeners() {
        client.realtime.connect()
        insertNoteLabelChannel.subscribe()
        deleteNoteLabelChannel.subscribe()
    }

    override suspend fun unsubscribeToRemoteNoteLabelListeners() {
        insertNoteLabelChannel.unsubscribe()
        deleteNoteLabelChannel.unsubscribe()
        client.realtime.disconnect()
    }

    override suspend fun createRemoteNoteLabelListener(): Flow<RemoteNoteLabel> {
        return insertNoteLabelChannel.postgresChangeFlow<PostgresAction.Insert>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.NoteLabels
        }.map { it.decodeRecord<RemoteNoteLabel>() }
    }

    override suspend fun deleteRemoteNoteLabelListener(): Flow<String> {
        return deleteNoteLabelChannel.postgresChangeFlow<PostgresAction.Delete>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.NoteLabels
        }.map { it.oldRecord.getValue(SupabaseConstants.Id).jsonPrimitive.content }
    }

}