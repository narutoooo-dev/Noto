package com.noto.app.data.label.source

import com.noto.app.data.label.model.RemoteLabel
import com.noto.app.data.util.SupabaseConstants
import com.noto.app.domain.tryCatching
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.jsonPrimitive

class SupabaseLabelClient(private val client: SupabaseClient) : RemoteLabelDataSource {

    private val insertLabelChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Label.Insert) }
    private val updateLabelChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Label.Update) }
    private val deleteLabelChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Label.Delete) }

    override suspend fun getAllRemoteLabels(): List<RemoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Labels]
                .select()
                .decodeList()
        }
    }

    override suspend fun getRemoteLabelsSince(timestamp: String): List<RemoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Labels]
                .select {
                    filter {
                        gt(SupabaseConstants.MetaDataUpdatedAtColumn, timestamp)
                    }
                }
                .decodeList()
        }
    }

    override suspend fun getRemoteLabelsByFolderId(remoteFolderId: String): List<RemoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Labels]
                .select {
                    filter {
                        RemoteLabel::folderId eq remoteFolderId
                    }
                }
                .decodeList()
        }
    }

    override suspend fun createRemoteLabel(remoteLabel: RemoteLabel) {
        client.postgrest[SupabaseConstants.Tables.Labels]
            .insert(remoteLabel)
    }

    override suspend fun updateRemoteLabel(remoteLabel: RemoteLabel) {
        client.postgrest[SupabaseConstants.Tables.Labels]
            .update(remoteLabel) {
                filter {
                    RemoteLabel::id eq remoteLabel.id
                }
            }
    }

    override suspend fun deleteRemoteLabelById(remoteLabelId: String) {
        client.postgrest[SupabaseConstants.Tables.Labels]
            .delete {
                filter {
                    RemoteLabel::id eq remoteLabelId
                }
            }
    }

    override suspend fun subscribeToRemoteLabelListeners() {
        client.realtime.connect()
        insertLabelChannel.subscribe()
        updateLabelChannel.subscribe()
        deleteLabelChannel.subscribe()
    }

    override suspend fun unsubscribeToRemoteLabelListeners() {
        insertLabelChannel.unsubscribe()
        updateLabelChannel.unsubscribe()
        deleteLabelChannel.unsubscribe()
        client.realtime.disconnect()
    }

    override suspend fun createRemoteLabelListener(): Flow<RemoteLabel> {
        return insertLabelChannel.postgresChangeFlow<PostgresAction.Insert>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Labels
        }.map { it.decodeRecord<RemoteLabel>() }
    }

    override suspend fun updateRemoteLabelListener(): Flow<RemoteLabel> {
        return updateLabelChannel.postgresChangeFlow<PostgresAction.Update>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Labels
        }.map { it.decodeRecord<RemoteLabel>() }
    }

    override suspend fun deleteRemoteLabelListener(): Flow<String> {
        return deleteLabelChannel.postgresChangeFlow<PostgresAction.Delete>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Labels
        }.map { it.oldRecord.getValue(SupabaseConstants.Id).jsonPrimitive.content }
    }

}