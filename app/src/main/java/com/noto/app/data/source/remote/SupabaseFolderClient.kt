package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteFolder
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.jsonPrimitive

class SupabaseFolderClient(private val client: SupabaseClient) : RemoteFolderDataSource {

    private val insertFolderChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Folder.Insert) }
    private val updateFolderChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Folder.Update) }
    private val deleteFolderChannel by lazy { client.realtime.channel(SupabaseConstants.RealtimeChannelIds.Folder.Delete) }

    override suspend fun getAllRemoteFolders(): List<RemoteFolder> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Folders]
                .select()
                .decodeList()
        }
    }

    override suspend fun getRemoteFoldersSince(timestamp: String): List<RemoteFolder> {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Folders]
                .select {
                    filter {
                        gt(SupabaseConstants.MetaDataUpdatedAtColumn, timestamp)
                    }
                }
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

    override suspend fun subscribeToRemoteFolderListeners() {
        client.realtime.connect()
        insertFolderChannel.subscribe()
        updateFolderChannel.subscribe()
        deleteFolderChannel.subscribe()
    }

    override suspend fun unsubscribeToRemoteFolderListeners() {
        insertFolderChannel.unsubscribe()
        updateFolderChannel.unsubscribe()
        deleteFolderChannel.unsubscribe()
        client.realtime.disconnect()
    }

    override suspend fun createRemoteFolderListener(): Flow<RemoteFolder> {
        return insertFolderChannel.postgresChangeFlow<PostgresAction.Insert>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Folders
        }.map { it.decodeRecord<RemoteFolder>() }
    }

    override suspend fun updateRemoteFolderListener(): Flow<RemoteFolder> {
        return updateFolderChannel.postgresChangeFlow<PostgresAction.Update>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Folders
        }.map { it.decodeRecord<RemoteFolder>() }
    }

    override suspend fun deleteRemoteFolderListener(): Flow<String> {
        return deleteFolderChannel.postgresChangeFlow<PostgresAction.Delete>(SupabaseConstants.Schemas.Public) {
            table = SupabaseConstants.Tables.Folders
        }.map { it.oldRecord.getValue(SupabaseConstants.Id).jsonPrimitive.content }
    }

}