package com.noto.app.data.user

import com.noto.app.crypto.KeyStoreManager
import com.noto.app.crypto.key.Argon2KeyGenerator
import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.folder.model.RemoteFolder
import com.noto.app.data.folder.source.RemoteFolderDataSource
import com.noto.app.data.label.model.RemoteLabel
import com.noto.app.data.label.source.RemoteLabelDataSource
import com.noto.app.data.note.label.model.RemoteNoteLabel
import com.noto.app.data.note.label.source.RemoteNoteLabelDataSource
import com.noto.app.data.note.model.RemoteNote
import com.noto.app.data.note.source.RemoteNoteDataSource
import com.noto.app.data.user.source.RemoteAuthDataSource
import com.noto.app.data.user.source.RemoteUserDataSource
import com.noto.app.domain.NotoException
import com.noto.app.domain.OtpType
import com.noto.app.domain.settings.SettingsRepository
import com.noto.app.domain.user.User
import com.noto.app.domain.user.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val remoteUserDataSource: RemoteUserDataSource,
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val remoteLabelDataSource: RemoteLabelDataSource,
    private val remoteNoteLabelDataSource: RemoteNoteLabelDataSource,
    private val settingsRepository: SettingsRepository,
    private val accountPasswordKeyGenerator: Argon2KeyGenerator,
    private val accountKekGenerator: Argon2KeyGenerator,
    private val keyStoreManager: KeyStoreManager,
    private val remoteFolderCacheHandler: RemoteItemCacheHandler<RemoteFolder>,
    private val remoteNoteCacheHandler: RemoteItemCacheHandler<RemoteNote>,
    private val remoteLabelCacheHandler: RemoteItemCacheHandler<RemoteLabel>,
    private val remoteNoteLabelCacheHandler: RemoteItemCacheHandler<RemoteNoteLabel>,
    private val coroutineDispatcher: CoroutineDispatcher,
) : UserRepository {

    override val user: Flow<Result<User>> = combine(
        settingsRepository.name.filterNotNull(),
        settingsRepository.email.filterNotNull(),
    ) { name, email -> User(name, email) }
        .map { Result.success(it) }
        .catch { emit(Result.failure(it)) }
        .flowOn(coroutineDispatcher)

    override suspend fun createAccount(name: String, email: String, password: String): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val isEmailExist = remoteAuthDataSource.isEmailExist(email)
            if (isEmailExist) {
                NotoException.Auth.UserAlreadyExists()
            } else {
                val passwordData = accountPasswordKeyGenerator.generateKey(password.encodeToByteArray())
                val encodedPassword = passwordData.key.let(accountPasswordKeyGenerator::encodeKeyToString)
                remoteAuthDataSource.signUp(name, email, encodedPassword, passwordData.encodedParameters)
                val keyData = accountKekGenerator.generateKey(password.encodeToByteArray())
                keyStoreManager.storeKey(KeyStoreManager.KeyEncryptionKeyId, keyData.key)
                settingsRepository.updateKeyEncryptionKeyParameters(keyData.encodedParameters)
            }
        }
    }

    override suspend fun logIn(email: String, password: String): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val passwordParametersResponse = remoteAuthDataSource.getPasswordParameters(email)
            val hashedPassword = accountPasswordKeyGenerator.generateKey(password.encodeToByteArray(), passwordParametersResponse.passwordParameters)
            val encodedPassword = accountPasswordKeyGenerator.encodeKeyToString(hashedPassword.key)
            remoteAuthDataSource.logIn(email, encodedPassword)
            val keyData = accountKekGenerator.generateKey(password.encodeToByteArray())
            keyStoreManager.storeKey(KeyStoreManager.KeyEncryptionKeyId, keyData.key)
            settingsRepository.updateKeyEncryptionKeyParameters(keyData.encodedParameters)
            finishLogin()
            fetchAndCacheRemoteItems()
        }
    }

    override suspend fun sendOtp(email: String, type: OtpType) = runCatching {
        when (type) {
            OtpType.LogIn -> remoteAuthDataSource.sendLogInOtp(email)
            OtpType.ChangeEmail -> remoteAuthDataSource.sendChangeEmailOtp(email)
            OtpType.DeleteAccount -> remoteAuthDataSource.sendDeleteAccountOtp(email)
        }
    }

    override suspend fun verifyOtp(email: String, type: OtpType, otp: String): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            when (type) {
                OtpType.LogIn -> {
                    remoteAuthDataSource.verifyLogInOtp(email, otp)
                    finishLogin()
                    fetchAndCacheRemoteItems()
                }

                OtpType.ChangeEmail -> {
                    remoteAuthDataSource.verifyChangeEmailOtp(email, otp)
                    settingsRepository.updateEmail(email)
                }

                OtpType.DeleteAccount -> remoteAuthDataSource.verifyDeleteAccountOtp(email, otp)
            }
        }
    }

    override suspend fun updateName(name: String): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val id = settingsRepository.id.filterNotNull().first()
            remoteUserDataSource.updateName(id, name)
            settingsRepository.updateName(name)
        }
    }

    override suspend fun updateEmail(email: String): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            remoteAuthDataSource.updateEmail(email)
        }
    }

    override suspend fun logOut(): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            remoteAuthDataSource.logOut()
        }
    }

    override suspend fun delete(): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            remoteAuthDataSource.delete()
        }
    }

    private suspend fun finishLogin() {
        val remoteUser = remoteUserDataSource.getUser()
        val remoteAuthUser = remoteAuthDataSource.get()
        settingsRepository.updateId(remoteUser.id)
        settingsRepository.updateName(remoteUser.name)
        remoteAuthUser?.let { settingsRepository.updateEmail(it.email) }
    }

    private suspend fun fetchAndCacheRemoteItems() {
        remoteFolderDataSource.getAllRemoteFolders().also { remoteFolderCacheHandler.cacheRemoteItems(it) }
        remoteNoteDataSource.getAllRemoteNotes().also { remoteNoteCacheHandler.cacheRemoteItems(it) }
        remoteLabelDataSource.getAllRemoteLabels().also { remoteLabelCacheHandler.cacheRemoteItems(it) }
        remoteNoteLabelDataSource.getAllRemoteNoteLabels().also { remoteNoteLabelCacheHandler.cacheRemoteItems(it) }
    }

}