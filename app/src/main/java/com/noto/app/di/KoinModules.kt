package com.noto.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.noto.app.AppViewModel
import com.noto.app.BuildConfig
import com.noto.app.crypto.*
import com.noto.app.crypto.key.Argon2KeyGenerator
import com.noto.app.crypto.key.DefaultArgon2KeyGenerator
import com.noto.app.crypto.key.PasswordBasedKeyGenerator
import com.noto.app.crypto.key.VaultPasscodeKeyGenerator
import com.noto.app.crypto.salt.EmptySaltGenerator
import com.noto.app.crypto.salt.SaltGenerator
import com.noto.app.crypto.salt.SecureRandomSaltGenerator
import com.noto.app.crypto.tink.TinkAesCryptoManager
import com.noto.app.crypto.tink.TinkCryptoManager
import com.noto.app.crypto.tink.TinkEncryptionHandler
import com.noto.app.data.GeneralFolderHandler
import com.noto.app.data.PropertyMapper
import com.noto.app.data.RemoteItemCacheHandler
import com.noto.app.data.database.NotoDatabase
import com.noto.app.data.folder.*
import com.noto.app.data.folder.model.RemoteFolder
import com.noto.app.data.folder.source.LocalEncryptedFolderDataSource
import com.noto.app.data.folder.source.LocalFolderDataSource
import com.noto.app.data.folder.source.RemoteFolderDataSource
import com.noto.app.data.folder.source.SupabaseFolderClient
import com.noto.app.data.label.*
import com.noto.app.data.label.model.RemoteLabel
import com.noto.app.data.label.source.LocalEncryptedLabelDataSource
import com.noto.app.data.label.source.LocalLabelDataSource
import com.noto.app.data.label.source.RemoteLabelDataSource
import com.noto.app.data.label.source.SupabaseLabelClient
import com.noto.app.data.note.*
import com.noto.app.data.note.label.NoteLabelMapper
import com.noto.app.data.note.label.RemoteNoteLabelCacheHandler
import com.noto.app.data.note.label.model.RemoteNoteLabel
import com.noto.app.data.note.label.source.LocalEncryptedNoteLabelDataSource
import com.noto.app.data.note.label.source.LocalNoteLabelDataSource
import com.noto.app.data.note.label.source.RemoteNoteLabelDataSource
import com.noto.app.data.note.label.source.SupabaseNoteLabelClient
import com.noto.app.data.note.model.RemoteNote
import com.noto.app.data.note.source.LocalEncryptedNoteDataSource
import com.noto.app.data.note.source.LocalNoteDataSource
import com.noto.app.data.note.source.RemoteNoteDataSource
import com.noto.app.data.note.source.SupabaseNoteClient
import com.noto.app.data.settings.SettingsRepositoryImpl
import com.noto.app.data.settings.source.LocalSettingsDataSource
import com.noto.app.data.sync.ManualSyncServiceManager
import com.noto.app.data.user.UserRepositoryImpl
import com.noto.app.data.user.source.RemoteAuthDataSource
import com.noto.app.data.user.source.RemoteUserDataSource
import com.noto.app.data.user.source.SupabaseAuthClient
import com.noto.app.data.user.source.SupabaseUserClient
import com.noto.app.data.util.SupabaseConstants
import com.noto.app.data.util.SupabaseDeepLinksHandler
import com.noto.app.data.vault.VaultRepositoryImpl
import com.noto.app.domain.folder.FolderRepository
import com.noto.app.domain.label.LabelRepository
import com.noto.app.domain.note.NoteRepository
import com.noto.app.domain.settings.SettingsRepository
import com.noto.app.domain.user.UserRepository
import com.noto.app.domain.vault.VaultRepository
import com.noto.app.ui.filtered.FilteredViewModel
import com.noto.app.ui.folder.FolderViewModel
import com.noto.app.ui.folder.NewFolderViewModel
import com.noto.app.ui.intro.IntroViewModel
import com.noto.app.ui.label.LabelViewModel
import com.noto.app.ui.label.NewLabelViewModel
import com.noto.app.ui.login.CreateAccountViewModel
import com.noto.app.ui.login.LoginViewModel
import com.noto.app.ui.login.VerifyOtpViewModel
import com.noto.app.ui.main.MainViewModel
import com.noto.app.ui.note.NotePagerViewModel
import com.noto.app.ui.note.NoteViewModel
import com.noto.app.ui.settings.SettingsViewModel
import com.noto.app.ui.settings.account.AccountSettingsViewModel
import com.noto.app.ui.settings.backup.AndroidLocalBackupHandler
import com.noto.app.ui.settings.backup.LocalBackupHandler
import com.noto.app.ui.settings.backup.LocalBackupSettingsViewModel
import com.noto.app.ui.settings.general.GeneralSettingsViewModel
import com.noto.app.ui.settings.readingmode.ReadingModeSettingsViewModel
import com.noto.app.ui.settings.vault.VaultSettingsViewModel
import com.noto.app.ui.settings.whatsnew.WhatsNewViewModel
import com.noto.app.ui.util.DeepLinksHandler
import com.noto.app.ui.vault.VaultPasscodeViewModel
import com.noto.app.widget.folder.FolderListWidgetConfigViewModel
import com.noto.app.widget.note.NoteListWidgetConfigViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import java.security.KeyStore

object KoinModules {

    private const val DataStoreName = "Noto Data Store"
    private val Context.dataStore by preferencesDataStore(name = DataStoreName)
    private const val AndroidKeyStore = "AndroidKeyStore"

    object Qualifiers {
        val CoroutineDispatcher = qualifier("CoroutineDispatcher")
        val CryptoJson = qualifier("CryptoJson")
        val RemoteFolderCacheHandler = qualifier("RemoteFolderCacheHandler")
        val RemoteNoteCacheHandler = qualifier("RemoteNoteCacheHandler")
        val RemoteLabelCacheHandler = qualifier("RemoteLabelCacheHandler")
        val RemoteNoteLabelCacheHandler = qualifier("RemoteNoteLabelCacheHandler")
        val AccountPasswordKeyGenerator = qualifier("AccountPasswordKeyGenerator")
        val AccountKekGenerator = qualifier("AccountKekGenerator")
        val VaultPasscodeKeyGenerator = qualifier("VaultPasscodeKeyGenerator")
        val BackupPasscodeKeyGenerator = qualifier("BackupPasscodeKeyGenerator")
        val SecureRandomSaltGenerator = qualifier("SecureRandomSaltGenerator")
        val EmptySaltGenerator = qualifier("EmptySaltGenerator")
    }

    val ViewModel = module {

        viewModel { MainViewModel(get(), get(), get()) }

        viewModel { FolderViewModel(get(), get(), get(), get(), get(), it.get(), it.getOrNull() ?: longArrayOf()) }

        viewModel { NoteViewModel(get(), get(), get(), get(), it[0], it[1], it.getOrNull(), it.getOrNull() ?: longArrayOf()) }

        viewModel { AppViewModel(get(), get(), get(), get(), get(), get(), get()) }

        viewModel { SettingsViewModel(get()) }

        viewModel { LabelViewModel(get(), get(), it[0], it[1]) }

        viewModel { FolderListWidgetConfigViewModel(it.get(), get(), get()) }

        viewModel { NoteListWidgetConfigViewModel(it.get(), get(), get(), get(), get()) }

        viewModel { NotePagerViewModel(get(), get(), get(), it[0], it[1], it[2], it[3]) }

        viewModel { FilteredViewModel(get(), get(), get(), it.get()) }

        viewModel { LoginViewModel(get(), get()) }

        viewModel { CreateAccountViewModel(get(), get()) }

        viewModel { VerifyOtpViewModel(get(), it[0], it[1], it[2]) }

        viewModel { NewFolderViewModel(get(), get()) }

        viewModel { NewLabelViewModel(get(), get(), it[0], it[1]) }

        viewModel { VaultSettingsViewModel(get(), get(), get(), get(Qualifiers.VaultPasscodeKeyGenerator)) }

        viewModel { VaultPasscodeViewModel(get(), get(Qualifiers.VaultPasscodeKeyGenerator)) }

        viewModel { AccountSettingsViewModel(get(), get(), get(), get(), get(), get()) }

        viewModel { ReadingModeSettingsViewModel(get()) }

        viewModel { GeneralSettingsViewModel(get(), get()) }

        viewModel { WhatsNewViewModel(get()) }

        viewModel { LocalBackupSettingsViewModel(get(), get()) }

        viewModel { IntroViewModel(get()) }

    }

    val Repository = module {

        single<FolderRepository> { FolderRepositoryImpl(get(), get(), get(), get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<NoteRepository> {
            NoteRepositoryImpl(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(Qualifiers.CoroutineDispatcher),
            )
        }

        single<LabelRepository> { LabelRepositoryImpl(get(), get(), get(), get(), get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<SettingsRepository> {
            SettingsRepositoryImpl(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(Qualifiers.BackupPasscodeKeyGenerator),
                get(),
                JsonConfigs.ExportImportData,
                get(Qualifiers.CoroutineDispatcher)
            )
        }

        single<UserRepository> {
            UserRepositoryImpl(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(Qualifiers.AccountPasswordKeyGenerator),
                get(Qualifiers.AccountKekGenerator),
                get(),
                get(Qualifiers.RemoteFolderCacheHandler),
                get(Qualifiers.RemoteNoteCacheHandler),
                get(Qualifiers.RemoteLabelCacheHandler),
                get(Qualifiers.RemoteNoteLabelCacheHandler),
                get(Qualifiers.CoroutineDispatcher),
            )
        }

        single<CoroutineDispatcher>(Qualifiers.CoroutineDispatcher) { Dispatchers.IO }

        single<GeneralFolderHandler> { GeneralFolderHandler(get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<VaultRepository> {
            VaultRepositoryImpl(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(Qualifiers.CoroutineDispatcher),
            )
        }

    }

    val LocalDataSource = module {

        single<LocalFolderDataSource> { NotoDatabase.getInstance(androidContext()).folderDao }

        single<LocalNoteDataSource> { NotoDatabase.getInstance(androidContext()).noteDao }

        single<LocalLabelDataSource> { NotoDatabase.getInstance(androidContext()).labelDao }

        single<LocalNoteLabelDataSource> { NotoDatabase.getInstance(androidContext()).noteLabelDao }

        single<DataStore<Preferences>> { androidContext().dataStore }

        single<LocalSettingsDataSource> { LocalSettingsDataSource(get()) }

        single<LocalEncryptedFolderDataSource> { NotoDatabase.getInstance(androidContext()).encryptedFolderDao }

        single<LocalEncryptedNoteDataSource> { NotoDatabase.getInstance(androidContext()).encryptedNoteDao }

        single<LocalEncryptedLabelDataSource> { NotoDatabase.getInstance(androidContext()).encryptedLabelDao }

        single<LocalEncryptedNoteLabelDataSource> { NotoDatabase.getInstance(androidContext()).encryptedNoteLabelDao }

    }

    val RemoteDataSource = module {

        single<SupabaseClient> {
            createSupabaseClient(SupabaseConstants.URLs.SupabaseUrl, BuildConfig.SupabaseApiKey) {
                defaultSerializer = KotlinXSerializer(JsonConfigs.Remote)
//                httpConfig {
//                    if (BuildConfig.DEBUG) {
//                        Logging {
//                            level = LogLevel.ALL
//                            logger = Logger.SIMPLE
//                        }
//                    }
//                }
                install(Auth) {
                    scheme = URLProtocol.HTTPS.name
                    host = SupabaseConstants.URLs.NotoHost
                }
                install(Postgrest)
                install(Realtime) {
                    connectOnSubscribe = false
                }
            }
        }

        single<RemoteAuthDataSource> { SupabaseAuthClient(get()) }

        single<RemoteUserDataSource> { SupabaseUserClient(get()) }

        single<DeepLinksHandler> { SupabaseDeepLinksHandler(get()) }

        single<RemoteFolderDataSource> { SupabaseFolderClient(get()) }

        single<RemoteNoteDataSource> { SupabaseNoteClient(get()) }

        single<RemoteLabelDataSource> { SupabaseLabelClient(get()) }

        single<RemoteNoteLabelDataSource> { SupabaseNoteLabelClient(get()) }

    }

    val Crypto = module {

        single<Argon2KeyGenerator>(Qualifiers.AccountPasswordKeyGenerator) { DefaultArgon2KeyGenerator(get(Qualifiers.SecureRandomSaltGenerator)) }

        single<Argon2KeyGenerator>(Qualifiers.AccountKekGenerator) {
            DefaultArgon2KeyGenerator(
                get(Qualifiers.SecureRandomSaltGenerator),
                saltSize = 0
            )
        }

        single<Argon2KeyGenerator>(Qualifiers.BackupPasscodeKeyGenerator) {
            DefaultArgon2KeyGenerator(
                get(Qualifiers.SecureRandomSaltGenerator),
                saltSize = 0
            )
        }

        single<PasswordBasedKeyGenerator>(Qualifiers.VaultPasscodeKeyGenerator) { VaultPasscodeKeyGenerator(get(Qualifiers.EmptySaltGenerator)) }

        single<TinkCryptoManager> { TinkAesCryptoManager() }

        single<Json>(Qualifiers.CryptoJson) { JsonConfigs.Crypto }

        single<KeyStoreManager> {
            val keyStore = KeyStore.getInstance(AndroidKeyStore).apply { load(null) }
            AndroidKeyStoreManager(keyStore)
        }

        single { TinkEncryptionHandler(get(), get(Qualifiers.CryptoJson)) }

        single<RawAesCryptoManager> { RawAesCryptoManager() }

        single<SaltGenerator>(Qualifiers.SecureRandomSaltGenerator) { SecureRandomSaltGenerator() }

        single<SaltGenerator>(Qualifiers.EmptySaltGenerator) { EmptySaltGenerator }

        single<RawAesEncryptionHandler> { RawAesEncryptionHandler(get(), get(Qualifiers.CryptoJson)) }

        single<VaultEncryptionHandler> { VaultEncryptionHandler(get(), get(Qualifiers.VaultPasscodeKeyGenerator), get()) }

    }

    val RemoteService = module {

        single<RemoteFolderService> { AndroidRemoteFolderService(androidContext().applicationContext) }

        single<RemoteNoteService> { AndroidRemoteNoteService(androidContext().applicationContext) }

        single<RemoteLabelService> { AndroidRemoteLabelService(androidContext().applicationContext) }

    }

    val LocalBackup = module {

        single<LocalBackupHandler> { AndroidLocalBackupHandler(androidApplication(), get(Qualifiers.CoroutineDispatcher)) }

    }

    val Mapper = module {

        single<FolderMapper> { FolderMapper(get(), get(), get(), get(), get(), get(), get(), get(), get()) }

        single<NoteMapper> { NoteMapper(get(), get(), get(), get(), get(), get(), get(), get()) }

        single<LabelMapper> { LabelMapper(get(), get(), get(), get(), get(), get(), get(), get()) }

        single<NoteLabelMapper> { NoteLabelMapper(get(), get(), get(), get(), get(), get(), get(), get()) }

        single<PropertyMapper> { PropertyMapper() }

    }

    val CacheHandler = module {

        single<RemoteItemCacheHandler<RemoteFolder>>(Qualifiers.RemoteFolderCacheHandler) { RemoteFolderCacheHandler(get(), get(), get()) }

        single<RemoteItemCacheHandler<RemoteNote>>(Qualifiers.RemoteNoteCacheHandler) { RemoteNoteCacheHandler(get(), get()) }

        single<RemoteItemCacheHandler<RemoteLabel>>(Qualifiers.RemoteLabelCacheHandler) { RemoteLabelCacheHandler(get(), get()) }

        single<RemoteItemCacheHandler<RemoteNoteLabel>>(Qualifiers.RemoteNoteLabelCacheHandler) { RemoteNoteLabelCacheHandler(get(), get()) }

    }

    val SyncService = module {

        single<FolderSyncService> { FolderSyncService(get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<NoteSyncService> { NoteSyncService(get(), get(), get(), get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<LabelSyncService> { LabelSyncService(get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<ManualSyncServiceManager> { ManualSyncServiceManager(get(), get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

    }

}