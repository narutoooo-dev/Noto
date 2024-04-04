package com.noto.app.data.user.source

import com.noto.app.data.user.model.PasswordParametersResponse
import com.noto.app.data.user.model.RemoteAuthUser
import kotlinx.coroutines.flow.Flow

interface RemoteAuthDataSource {

    val isUserLoggedIn: Flow<Boolean>

    suspend fun signUp(name: String, email: String, password: String, passwordParameters: String): String

    suspend fun logIn(email: String, password: String)

    suspend fun sendLogInOtp(email: String)

    suspend fun sendChangeEmailOtp(email: String)

    suspend fun sendDeleteAccountOtp(email: String)

    suspend fun verifyLogInOtp(email: String, otp: String)

    suspend fun verifyChangeEmailOtp(email: String, otp: String)

    suspend fun verifyDeleteAccountOtp(email: String, otp: String)

    suspend fun isEmailExist(email: String): Boolean

    suspend fun updateEmail(email: String)

    suspend fun get(): RemoteAuthUser?

    suspend fun retrieve(): RemoteAuthUser

    suspend fun logOut()

    suspend fun delete()

    suspend fun getPasswordParameters(email: String): PasswordParametersResponse

}