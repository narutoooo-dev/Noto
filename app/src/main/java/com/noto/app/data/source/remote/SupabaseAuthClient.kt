package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteAuthUser
import com.noto.app.data.model.remote.response.IsEmailExistResponse
import com.noto.app.data.model.remote.response.PasswordParametersResponse
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.model.tryCatching
import com.noto.app.domain.model.unknownException
import com.noto.app.domain.source.remote.RemoteAuthDataSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthClient(private val client: SupabaseClient) : RemoteAuthDataSource {

    override suspend fun signUp(name: String, email: String, password: String, passwordParameters: String): String {
        return tryCatching(
            onException = { exception ->
                when (exception) {
                    is RestException -> when (exception.description) {
                        "Unable to validate email address: invalid format" -> NotoException.Auth.InvalidEmail()
                        "Password should be at least 8 characters" -> NotoException.Auth.InvalidPassword()
                        "User already registered" -> NotoException.Auth.UserAlreadyExists()
                        "Email rate limit exceeded" -> NotoException.TryAgainLater()
                        else -> {
                            if (exception.message?.contains("seconds") == true) {
                                NotoException.TryAgainLater()
                            } else {
                                unknownException(exception.message)
                            }
                        }
                    }

                    else -> unknownException(exception.message)
                }
            }
        ) {
            val result = client.gotrue.signUpWith(Email, SupabaseConstants.URLs.NotoVerifyEmail) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put(SupabaseConstants.Name, name)
                    put(SupabaseConstants.PasswordParameters, passwordParameters)
                }
            }
            result?.id ?: error("SIGNUP")
        }
    }

    override suspend fun logIn(email: String, password: String) {
        tryCatching(
            onException = { exception ->
                when (exception) {
                    is RestException -> when (exception.description) {
                        "Invalid login credentials" -> NotoException.Auth.InvalidCredentials()
                        "Email not confirmed" -> NotoException.Auth.EmailNotVerified()
                        else -> unknownException(exception.message)
                    }

                    else -> unknownException(exception.message)
                }
            }
        ) {
            client.gotrue.loginWith(Email, SupabaseConstants.URLs.NotoVerifyEmail) {
                this.email = email
                this.password = password
            }
        }
    }

    override suspend fun verifyEmail(email: String) {
        tryCatching {
            client.gotrue.resendEmail(OtpType.Email.SIGNUP, email)
        }
    }

    override suspend fun isEmailExist(email: String): Boolean {
        return tryCatching {
            val parameters = mapOf(SupabaseConstants.Email to email)
            client.postgrest.rpc(SupabaseConstants.RPCs.IsEmailExist, parameters).decodeAs<IsEmailExistResponse>().isExist
        }
    }

    override suspend fun updateEmail(email: String) {
        tryCatching(
            onException = { exception ->
                when (exception) {
                    is RestException -> when (exception.description) {
                        "Unable to validate email address: invalid format" -> NotoException.Auth.InvalidEmail()
                        "A user with this email address has already been registered" -> NotoException.Auth.UserAlreadyExists()
                        else -> unknownException(exception.message)
                    }

                    else -> unknownException(exception.message)
                }
            }
        ) {
            // TODO Add redirect modifier to SupabaseConstants.URLs.NotoVerifyEmail
            client.gotrue.modifyUser { this.email = email }
        }
    }

    override suspend fun get(): RemoteAuthUser {
        return tryCatching {
            with(client.gotrue.retrieveUserForCurrentSession(updateSession = true)) {
                RemoteAuthUser(
                    id,
                    email.toString(),
                    confirmationSentAt.toString(),
                    createdAt.toString(),
                    updatedAt.toString(),
                )
            }
        }
    }

    override suspend fun logOut() {
        tryCatching {
            client.gotrue.logout()
        }
    }

    override suspend fun delete() {
        tryCatching {
            client.postgrest.rpc(SupabaseConstants.RPCs.DeleteUser)
        }
    }

    override suspend fun getPasswordParameters(email: String): PasswordParametersResponse {
        return tryCatching(
            onException = { NotoException.Auth.InvalidCredentials() }
        ) {
            val parameters = mapOf(SupabaseConstants.Email to email)
            client.postgrest.rpc(SupabaseConstants.RPCs.GetPasswordParameters, parameters)
                .decodeAs<PasswordParametersResponse>()
        }
    }

}