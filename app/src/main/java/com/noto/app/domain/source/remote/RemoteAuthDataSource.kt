package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteAuthUser
import com.noto.app.data.model.remote.response.PasswordParametersResponse
import kotlinx.coroutines.flow.Flow

interface RemoteAuthDataSource {

    val isUserLoggedIn: Flow<Boolean>

    suspend fun signUp(name: String, email: String, password: String, passwordParameters: String): String

    suspend fun logIn(email: String, password: String)

    suspend fun verifyEmail(email: String)

    suspend fun isEmailExist(email: String): Boolean

    suspend fun updateEmail(email: String)

    suspend fun get(): RemoteAuthUser

    suspend fun retrieve(): RemoteAuthUser

    suspend fun logOut()

    suspend fun delete()

    suspend fun getPasswordParameters(email: String): PasswordParametersResponse

}