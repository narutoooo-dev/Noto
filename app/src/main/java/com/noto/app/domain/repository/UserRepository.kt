package com.noto.app.domain.repository

import com.noto.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val user: Flow<Result<User>>

    suspend fun createAccount(name: String, email: String, password: String): Result<Unit>

    suspend fun logIn(email: String, password: String): Result<Unit>

    suspend fun finishCreatingAccount(id: String, email: String): Result<Unit>

    suspend fun updateName(name: String): Result<Unit>

    suspend fun updateEmail(email: String): Result<Unit>

    suspend fun finishUpdatingEmail(email: String): Result<Unit>

    suspend fun logOut(): Result<Unit>

    suspend fun delete(): Result<Unit>

}