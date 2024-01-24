package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteUser
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.source.remote.RemoteUserDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupabaseUserClient(private val client: SupabaseClient) : RemoteUserDataSource {

    override suspend fun getUser(): RemoteUser {
        return tryCatching {
            client.postgrest[SupabaseConstants.Tables.Users].select { single() }.decodeAs()
        }
    }

    override suspend fun updateName(id: String, name: String) {
        tryCatching {
            client.postgrest[SupabaseConstants.Tables.Users].update(
                update = { RemoteUser::name setTo name },
            ) {
                filter {
                    RemoteUser::id eq id
                }
            }
        }
    }

}