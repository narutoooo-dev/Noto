package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.response.PasswordParametersResponse
import com.noto.app.data.model.remote.RemoteAuthUser

interface RemoteAuthDataSource {

    suspend fun signUp(name: String, email: String, password: String, passwordParameters: String): String

    suspend fun login(email: String, password: String)

    suspend fun verifyEmail(email: String)

    suspend fun isEmailExist(email: String): Boolean

    suspend fun updateEmail(email: String)

    suspend fun get(): RemoteAuthUser

    suspend fun logOut()

    suspend fun delete()

    suspend fun getPasswordParameters(email: String): PasswordParametersResponse

}