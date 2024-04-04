package com.noto.app.domain.user

import com.noto.app.domain.OtpType
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val user: Flow<Result<User>>

    suspend fun createAccount(name: String, email: String, password: String): Result<Unit>

    suspend fun logIn(email: String, password: String): Result<Unit>

    suspend fun sendOtp(email: String, type: OtpType): Result<Unit>

    suspend fun verifyOtp(email: String, type: OtpType, otp: String): Result<Unit>

    suspend fun updateName(name: String): Result<Unit>

    suspend fun updateEmail(email: String): Result<Unit>

    suspend fun logOut(): Result<Unit>

    suspend fun delete(): Result<Unit>

}