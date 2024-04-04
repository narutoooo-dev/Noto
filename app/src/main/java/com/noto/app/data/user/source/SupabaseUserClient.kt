package com.noto.app.data.user.source

import com.noto.app.data.user.model.RemoteUser
import com.noto.app.data.util.SupabaseConstants
import com.noto.app.domain.tryCatching
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