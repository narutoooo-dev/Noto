package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteLabel
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteLabelDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning

class SupabaseLabelClient(private val client: SupabaseClient) : RemoteLabelDataSource {

    override suspend fun getRemoteLabelsByFolderId(remoteFolderId: String): List<RemoteLabel> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Labels]
                .select { RemoteLabel::folderId eq remoteFolderId }
                .decodeList()
        }
    }

    override suspend fun createRemoteLabel(remoteLabel: RemoteLabel) {
        client.postgrest[SupabaseConstants.Tables.Labels]
            .insert(remoteLabel, returning = Returning.MINIMAL)
    }

    override suspend fun updateRemoteLabel(remoteLabel: RemoteLabel) {
        client.postgrest[SupabaseConstants.Tables.Labels]
            .update(remoteLabel, returning = Returning.MINIMAL) { RemoteLabel::id eq remoteLabel.id }
    }

    override suspend fun deleteRemoteLabelById(remoteLabelId: String) {
        client.postgrest[SupabaseConstants.Tables.Labels]
            .delete(returning = Returning.MINIMAL) { RemoteLabel::id eq remoteLabelId }
    }

}