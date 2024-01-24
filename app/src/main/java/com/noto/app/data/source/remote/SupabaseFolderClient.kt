package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupabaseFolderClient(private val client: SupabaseClient) : RemoteFolderDataSource {

    override suspend fun getAllRemoteFolders(): List<RemoteFolder> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Folders]
                .select()
                .decodeList()
        }
    }

    override suspend fun createRemoteFolder(remoteFolder: RemoteFolder) {
        client.postgrest[SupabaseConstants.Tables.Folders]
            .insert(remoteFolder)
    }

    override suspend fun updateRemoteFolder(remoteFolder: RemoteFolder) {
        client.postgrest[SupabaseConstants.Tables.Folders]
            .update(remoteFolder) {
                filter {
                    RemoteFolder::id eq remoteFolder.id
                }
            }
    }

    override suspend fun deleteRemoteFolderById(remoteFolderId: String) {
        client.postgrest[SupabaseConstants.Tables.Folders]
            .delete {
                filter {
                    RemoteFolder::id eq remoteFolderId
                }
            }
    }

}