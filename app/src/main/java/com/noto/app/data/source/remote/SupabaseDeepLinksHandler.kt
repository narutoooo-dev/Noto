package com.noto.app.data.source.remote

import android.content.Intent
import com.noto.app.domain.model.DeepLinksHandler
import com.noto.app.domain.model.tryCatching
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.handleDeeplinks

class SupabaseDeepLinksHandler(private val client: SupabaseClient) : DeepLinksHandler {

    override fun handleDeepLinks(
        intent: Intent,
        onFinishCreatingAccount: (id: String, email: String) -> Unit,
        onFinishUpdatingEmail: (email: String) -> Unit
    ) {
        tryCatching {
            val uri = intent.data
            val fragmentParameters = uri?.fragment?.asUrlParameters() ?: emptyMap()
            val type = fragmentParameters[SupabaseConstants.Type]
            val newEmail = uri?.getQueryParameter(SupabaseConstants.Email)
            client.handleDeeplinks(intent) { session ->
                when {
                    type == SupabaseConstants.SignUp -> onFinishCreatingAccount(session.user?.id!!, session.user?.email!!)
                    type == SupabaseConstants.EmailChange && newEmail != null -> onFinishUpdatingEmail(newEmail)
                }
            }
        }
    }

    private fun String.asUrlParameters(): Map<String, String> {
        val parameterDelimiter = '&'
        val keyValueDelimiter = '='
        return split(parameterDelimiter).associate { parameter ->
            val key = parameter.substringBefore(keyValueDelimiter)
            val value = parameter.substringAfter(keyValueDelimiter)
            key to value
        }
    }

}