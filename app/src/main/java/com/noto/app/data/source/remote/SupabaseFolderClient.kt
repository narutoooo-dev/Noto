package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning

class SupabaseFolderClient(private val client: SupabaseClient) : RemoteFolderDataSource {

    override suspend fun getRemoteFolders(): List<RemoteFolder> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Folders]
                .select()
                .decodeList()
        }
    }

    override suspend fun createRemoteFolder(remoteFolder: RemoteFolder) {
        client.postgrest[SupabaseConstants.Tables.Folders]
            .insert(remoteFolder, returning = Returning.MINIMAL)
    }

    override suspend fun updateRemoteFolder(remoteFolder: RemoteFolder) {
        client.postgrest[SupabaseConstants.Tables.Folders]
            .update(remoteFolder, returning = Returning.MINIMAL) { RemoteFolder::id eq remoteFolder.id }
    }

    override suspend fun deleteRemoteFolderById(remoteFolderId: String) {
        client.postgrest[SupabaseConstants.Tables.Folders]
            .delete(returning = Returning.MINIMAL) { RemoteFolder::id eq remoteFolderId }
    }

    override suspend fun getRemoteGeneralFolderOrNull(): RemoteFolder? {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Folders]
                .select { RemoteFolder::title eq "" }
                .decodeSingleOrNull()
        }
    }

}