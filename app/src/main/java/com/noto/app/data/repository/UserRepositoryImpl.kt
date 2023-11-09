package com.noto.app.data.repository

import com.noto.app.crypto.KeyStoreManager
import com.noto.app.crypto.PasswordTransformer
import com.noto.app.domain.model.NotoException
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
    private val keyStoreManager: KeyStoreManager,
) : UserRepository {

    override val user: Flow<Result<User>> = combine(
        settingsRepository.name.filterNotNull(),
        settingsRepository.email.filterNotNull(),
    ) { name, email -> User(name, email) }
        .map { Result.success(it) }
        .catch { emit(Result.failure(it)) }
        .flowOn(dispatcher)

    override suspend fun createAccount(name: String, email: String, password: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val isEmailExist = remoteAuthDataSource.isEmailExist(email)
            if (isEmailExist) {
                NotoException.Auth.UserAlreadyExists()
            } else {
                val passwordData = passwordTransformer.hashPassword(password.toByteArray())
                val encodedPassword = passwordData.key.let(passwordTransformer::encodeToString)
                remoteAuthDataSource.signUp(name, email, encodedPassword, passwordData.encodedParameters)
                val keyData = passwordTransformer.generateKEK(password.encodeToByteArray())
                keyStoreManager.storeKEK(keyData.key, keyData.encodedParameters)
            }
        }
    }

    override suspend fun logIn(email: String, password: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val passwordParametersResponse = remoteAuthDataSource.getPasswordParameters(email)
            val hashedPassword = passwordTransformer.verifyPassword(password.toByteArray(), passwordParametersResponse.passwordParameters)
            val encodedPassword = passwordTransformer.encodeToString(hashedPassword)
            remoteAuthDataSource.logIn(email, encodedPassword)
            val keyData = passwordTransformer.generateKEK(password.encodeToByteArray())
            keyStoreManager.storeKEK(keyData.key, keyData.encodedParameters)
            val remoteAuthUser = remoteAuthDataSource.get()
            finishCreatingAccount(remoteAuthUser.id, remoteAuthUser.email).getOrThrow()
        }
    }.recoverCatching { exception ->
        withContext(dispatcher) {
            if (exception is NotoException.Auth.EmailNotVerified) remoteAuthDataSource.verifyEmail(email)
            throw exception
        }
    }

    override suspend fun finishCreatingAccount(id: String, email: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            settingsRepository.updateId(id)
            settingsRepository.updateEmail(email)
            val user = remoteUserDataSource.getUser()
            settingsRepository.updateName(user.name)
        }
    }

    override suspend fun updateName(name: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            val id = settingsRepository.id.filterNotNull().first()
            remoteUserDataSource.updateName(id, name)
            settingsRepository.updateName(name)
        }
    }

    override suspend fun updateEmail(email: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            remoteAuthDataSource.updateEmail(email)
        }
    }

    override suspend fun finishUpdatingEmail(email: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            settingsRepository.updateEmail(email)
        }
    }

    override suspend fun logOut(): Result<Unit> = runCatching {
        withContext(dispatcher) {
            remoteAuthDataSource.logOut()
        }
    }

    override suspend fun delete(): Result<Unit> = runCatching {
        withContext(dispatcher) {
            remoteAuthDataSource.delete()
        }
    }

}