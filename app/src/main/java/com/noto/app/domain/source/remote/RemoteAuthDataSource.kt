package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.AuthResponse
import com.noto.app.data.model.remote.PasswordParametersResponse
import com.noto.app.data.model.remote.RemoteAuthUser
import com.noto.app.data.model.remote.response.IsEmailExistResponse

interface RemoteAuthDataSource {

    suspend fun signUp(name: String, email: String, password: String, passwordParameters: String): RemoteAuthUser

    suspend fun login(email: String, password: String): AuthResponse

    suspend fun verifyEmail(email: String)

    suspend fun refreshToken(refreshToken: String): AuthResponse

    suspend fun isEmailExist(email: String): IsEmailExistResponse

    suspend fun updateEmail(email: String): RemoteAuthUser

    suspend fun get(): RemoteAuthUser

    suspend fun logOut()

    suspend fun delete()

    suspend fun getPasswordParameters(email: String): PasswordParametersResponse

}