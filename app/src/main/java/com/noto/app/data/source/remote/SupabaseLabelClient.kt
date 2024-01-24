package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteLabel
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteLabelDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupabaseLabelClient(private val client: SupabaseClient) : RemoteLabelDataSource {

    override suspend fun getAllRemoteLabels(): List<RemoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Labels]
                .select()
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

}