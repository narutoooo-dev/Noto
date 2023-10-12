package com.noto.app.data.repository

import com.noto.app.crypto.PasswordTransformer
import com.noto.app.data.model.remote.ResponseException
import com.noto.app.domain.model.User
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.repository.UserRepository
import com.noto.app.domain.source.remote.RemoteAuthDataSource
import com.noto.app.domain.source.remote.RemoteUserDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val remoteUserDataSource: RemoteUserDataSource,
    private val settingsRepository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val passwordTransformer: PasswordTransformer,
) : UserRepository {

    override val user: Flow<Result<User>> = combine(
        settingsRepository.name,
        settingsRepository.email,
    ) { name, email -> User(name, email) }
        .map { Result.success(it) }
        .catch { emit(Result.failure(it)) }
        .flowOn(dispatcher)

    override suspend fun createAccount(name: String, email: String, password: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val isEmailExist = remoteAuthDataSource.isEmailExist(email).isExist
            if (isEmailExist) {
                ResponseException.Auth.UserAlreadyExists()
            } else {
                val passwordData = passwordTransformer.hashPassword(password.toByteArray())
                val encodedPassword = passwordData.key.let(passwordTransformer::encodeToString)
                remoteAuthDataSource.signUp(name, email, encodedPassword, passwordData.encodedParameters)
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val passwordParametersResponse = remoteAuthDataSource.getPasswordParameters(email)
            val hashedPassword = passwordTransformer.verifyPassword(password.toByteArray(), passwordParametersResponse.passwordParameters)
            val encodedPassword = passwordTransformer.encodeToString(hashedPassword)
            val authResponse = remoteAuthDataSource.login(email, encodedPassword)
            finishCreatingAccount(authResponse.accessToken, authResponse.refreshToken).getOrThrow()
        }
    }.recoverCatching { exception ->
        withContext(dispatcher) {
            if (exception is ResponseException.Auth.EmailNotVerified) remoteAuthDataSource.verifyEmail(email)
            throw exception
        }
    }

    override suspend fun finishCreatingAccount(accessToken: String, refreshToken: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            settingsRepository.updateAccessToken(accessToken)
            settingsRepository.updateRefreshToken(refreshToken)
            val authUser = remoteAuthDataSource.get()
            val user = remoteUserDataSource.getUser()
            settingsRepository.updateId(user.id)
            settingsRepository.updateName(user.name)
            settingsRepository.updateEmail(authUser.email)
        }
    }

    override suspend fun updateName(name: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val id = settingsRepository.id.first()
            remoteUserDataSource.updateName(id, name)
            settingsRepository.updateName(name)
        }
    }

    override suspend fun updateEmail(email: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            remoteAuthDataSource.updateEmail(email)
        }
    }

    override suspend fun finishUpdatingEmail(
        email: String,
        accessToken: String,
        refreshToken: String,
    ): Result<Unit> = runCatching {
        withContext(dispatcher) {
            settingsRepository.updateEmail(email)
        }
    }

    override suspend fun logOutUser(): Result<Unit> = runCatching {
        withContext(dispatcher) {
            remoteAuthDataSource.logOut()
        }
    }

    override suspend fun deleteUser(): Result<Unit> = runCatching {
        withContext(dispatcher) {
            remoteAuthDataSource.delete()
        }
    }

}