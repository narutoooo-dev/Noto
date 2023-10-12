package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.*
import com.noto.app.data.model.remote.ResponseException.Auth
import com.noto.app.data.model.remote.request.*
import com.noto.app.data.model.remote.response.IsEmailExistResponse
import com.noto.app.domain.source.remote.RemoteAuthDataSource
import com.noto.app.util.Constants
import com.noto.app.util.getOrElse
import com.noto.app.util.unhandledError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode

class RemoteAuthClient(private val authClient: HttpClient, private val client: HttpClient) : RemoteAuthDataSource {

    override suspend fun signUp(name: String, email: String, password: String, passwordParameters: String): RemoteAuthUser {
        return authClient.post("/auth/v1/signup") {
            parameter(Constants.RedirectTo, "https://noto.dev/verify")
            setBody(SignUpRequest(email, password, UserMetadata(name, passwordParameters)))
        }.getOrElse { response ->
            val errorResponse = response.body<SignUpErrorResponse>()
            when (response.status) {
                HttpStatusCode.BadRequest -> Auth.UserAlreadyExists()
                HttpStatusCode.UnprocessableEntity -> when {
                    errorResponse.msg.contains("email", ignoreCase = true) -> Auth.InvalidEmail()
                    errorResponse.msg.contains("password", ignoreCase = true) -> Auth.InvalidPassword()
                    else -> unhandledError(errorResponse.msg)
                }

                HttpStatusCode.TooManyRequests -> {
                    val seconds = errorResponse.msg.substringAfter("after ")
                        .substringBefore("seconds")
                        .trim()
                        .toInt()
                    Auth.TooManyRequests(seconds).invoke()
                }

                else -> unhandledError(errorResponse.msg)
            }
        }
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        return authClient.post("/auth/v1/token") {
            parameter(Constants.GrantType, Constants.Password)
            setBody(LoginRequest(email, password))
        }.getOrElse { response ->
            val errorResponse = response.body<AuthErrorResponse>()
            when (response.status) {
                HttpStatusCode.BadRequest -> {
                    val errorDescription = errorResponse.errorDescription
                    if (errorDescription.contains("email", ignoreCase = true))
                        Auth.EmailNotVerified()
                    else if (errorDescription.contains("credentials", ignoreCase = true))
                        Auth.InvalidLoginCredentials()
                    else
                        unhandledError(errorResponse.error)
                }

                else -> unhandledError(errorResponse.error)
            }
        }
    }

    override suspend fun verifyEmail(email: String) {
        return authClient.post("/auth/v1/resend") {
            parameter(Constants.RedirectTo, "https://noto.dev/verify")
            setBody(VerifyEmailRequest(Constants.SignUp, email))
        }.getOrElse { response ->
            val errorResponse = response.body<RestErrorResponse>()
            unhandledError(errorResponse.message)
        }
    }

    override suspend fun refreshToken(refreshToken: String): AuthResponse {
        return authClient.post("/auth/v1/token") {
            parameter(Constants.GrantType, Constants.RefreshToken)
            setBody(RefreshTokenRequest(refreshToken))
        }.getOrElse { response ->
            val errorResponse = response.body<AuthErrorResponse>()
            when (response.status) {
                HttpStatusCode.BadRequest -> Auth.InvalidRefreshToken()
                else -> unhandledError(errorResponse.error)
            }
        }
    }

    override suspend fun isEmailExist(email: String): IsEmailExistResponse {
        return authClient.get("/rest/v1/rpc/is_email_exist") {
            parameter(Constants.Email, email)
        }.getOrElse { response ->
            val errorResponse = response.body<RestErrorResponse>()
            unhandledError(errorResponse.message)
        }
    }

    override suspend fun updateEmail(email: String): RemoteAuthUser {
        return client.put("/auth/v1/user") {
            parameter(Constants.RedirectTo, "https://noto.dev/verify")
            setBody(UpdateEmailRequest(email))
        }.getOrElse { response ->
            val errorResponse = response.body<SignUpErrorResponse>()
            when (response.status) {
                HttpStatusCode.UnprocessableEntity -> {
                    if (errorResponse.msg.contains("format"))
                        Auth.InvalidEmail()
                    else if (errorResponse.msg.contains("exists"))
                        Auth.UserAlreadyExists()
                    else
                        unhandledError(errorResponse.msg)
                }

                else -> unhandledError(errorResponse.msg)
            }
        }
    }

    override suspend fun get(): RemoteAuthUser {
        return client.get("/auth/v1/user").getOrElse { response ->
            val errorResponse = response.body<AuthErrorResponse>()
            unhandledError(errorResponse.error)
        }
    }

    override suspend fun logOut() {
        return client.post("/auth/v1/logout").getOrElse { response ->
            val errorResponse = response.body<AuthErrorResponse>()
            unhandledError(errorResponse.error)
        }
    }

    override suspend fun delete() {
        return client.post("/rest/v1/rpc/delete_user").getOrElse { response ->
            val errorResponse = response.body<AuthErrorResponse>()
            unhandledError(errorResponse.error)
        }
    }

    override suspend fun getPasswordParameters(email: String): PasswordParametersResponse {
        return authClient.post("/rest/v1/rpc/get_password_parameters") {
            setBody(GetPasswordParametersRequest(email))
        }.getOrElse { Auth.InvalidLoginCredentials() }
    }

}