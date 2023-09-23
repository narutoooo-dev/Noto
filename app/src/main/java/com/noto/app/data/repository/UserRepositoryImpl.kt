package com.noto.app.data.repository

import com.noto.app.crypto.PasswordTransformer
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

    override suspend fun registerUser(name: String, email: String, password: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val passwordData = passwordTransformer.hashPassword(password.toByteArray())
            val hashedPassword = passwordData.key.let(passwordTransformer::encodeToString)
            val remoteAuthUser = remoteAuthDataSource.signUp(email, hashedPassword)
            settingsRepository.updateId(remoteAuthUser.id)
            settingsRepository.updateName(name)
            settingsRepository.updateEmail(email)
            settingsRepository.updatePasswordParameters(passwordData.encodedParameters)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val passwordParametersResponse = remoteAuthDataSource.getPasswordParameters(email)
            val hashedPassword = passwordTransformer.verifyPassword(
                password = password.toByteArray(),
                encodedParameters = passwordParametersResponse.passwordParameters,
            ).let(passwordTransformer::encodeToString)
            val response = remoteAuthDataSource.login(email, hashedPassword)
            settingsRepository.updateId(response.user.id)
            settingsRepository.updateEmail(response.user.email)
            settingsRepository.updateAccessToken(response.accessToken)
            settingsRepository.updateRefreshToken(response.refreshToken)
            val user = remoteUserDataSource.getUser()
            settingsRepository.updateName(user.name)
        }
    }

    override suspend fun completeUserRegistration(accessToken: String, refreshToken: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val id = settingsRepository.id.first()
            val name = settingsRepository.name.first()
            val passwordParameters = settingsRepository.passwordParameters.first()
            settingsRepository.updateAccessToken(accessToken)
            settingsRepository.updateRefreshToken(refreshToken)
            remoteUserDataSource.createUser(id, name, passwordParameters)
            settingsRepository.clearPasswordParameters()
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

    override suspend fun completeUpdatingEmail(
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