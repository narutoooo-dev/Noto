package com.noto.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.noto.app.AppViewModel
import com.noto.app.BuildConfig
import com.noto.app.crypto.*
import com.noto.app.data.database.NotoDatabase
import com.noto.app.data.model.local.LocalGeneralFolderManager
import com.noto.app.data.repository.*
import com.noto.app.data.service.AndroidRemoteFolderService
import com.noto.app.data.service.AndroidRemoteLabelService
import com.noto.app.data.service.AndroidRemoteNoteService
import com.noto.app.data.source.remote.*
import com.noto.app.domain.model.DeepLinksHandler
import com.noto.app.domain.repository.*
import com.noto.app.domain.service.RemoteFolderService
import com.noto.app.domain.service.RemoteLabelService
import com.noto.app.domain.service.RemoteNoteService
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import com.noto.app.domain.source.remote.*
import com.noto.app.filtered.FilteredViewModel
import com.noto.app.folder.FolderViewModel
import com.noto.app.folder.NewFolderViewModel
import com.noto.app.intro.IntroViewModel
import com.noto.app.label.LabelViewModel
import com.noto.app.label.NewLabelViewModel
import com.noto.app.login.CreateAccountViewModel
import com.noto.app.login.LoginViewModel
import com.noto.app.login.VerifyOtpViewModel
import com.noto.app.main.MainViewModel
import com.noto.app.note.NotePagerViewModel
import com.noto.app.note.NoteViewModel
import com.noto.app.settings.SettingsViewModel
import com.noto.app.settings.account.AccountSettingsViewModel
import com.noto.app.settings.backup.AndroidLocalBackupHandler
import com.noto.app.settings.backup.LocalBackupHandler
import com.noto.app.settings.backup.LocalBackupSettingsViewModel
import com.noto.app.settings.general.GeneralSettingsViewModel
import com.noto.app.settings.readingmode.ReadingModeSettingsViewModel
import com.noto.app.settings.vault.VaultSettingsViewModel
import com.noto.app.settings.whatsnew.WhatsNewViewModel
import com.noto.app.vault.VaultPasscodeViewModel
import com.noto.app.widget.FolderListWidgetConfigViewModel
import com.noto.app.widget.NoteListWidgetConfigViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
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
    }

    val ViewModel = module {

        viewModel { MainViewModel(get(), get(), get()) }

        viewModel { FolderViewModel(get(), get(), get(), get(), it.get(), it.getOrNull() ?: longArrayOf()) }

        viewModel { NoteViewModel(get(), get(), get(), get(), it[0], it[1], it.getOrNull(), it.getOrNull() ?: longArrayOf()) }

        viewModel { AppViewModel(get(), get(), get()) }

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

        viewModel { VaultSettingsViewModel(get(), get()) }

        viewModel { VaultPasscodeViewModel(get()) }

        viewModel { AccountSettingsViewModel(get(), get(), get(), get(), get(), get()) }

        viewModel { ReadingModeSettingsViewModel(get()) }

        viewModel { GeneralSettingsViewModel(get(), get()) }

        viewModel { WhatsNewViewModel(get()) }

        viewModel { LocalBackupSettingsViewModel(get(), get()) }

        viewModel { IntroViewModel(get()) }

    }

    val Repository = module {

        single<FolderRepository> { FolderRepositoryImpl(get(), get(), get(), get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<NoteRepository> { NoteRepositoryImpl(get(), get(), get(), get(), get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<LabelRepository> { LabelRepositoryImpl(get(), get(), get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

        single<SettingsRepository> {
            SettingsRepositoryImpl(
                get(),
                get(),
                get(),
                get(),
                get(),
                JsonConfigs.ExportImportData,
                get(Qualifiers.CoroutineDispatcher)
            )
        }

        single<UserRepository> { UserRepositoryImpl(get(), get(), get(), passwordTransformer = get(), keyStoreManager = get()) }

        single<CoroutineDispatcher>(Qualifiers.CoroutineDispatcher) { Dispatchers.IO }

        single<LocalGeneralFolderManager> { FolderRepositoryImpl(get(), get(), get(), get(), get(), get(), get(Qualifiers.CoroutineDispatcher)) }

    }

    val LocalDataSource = module {

        single<LocalFolderDataSource> { NotoDatabase.getInstance(androidContext()).folderDao }

        single<LocalNoteDataSource> { NotoDatabase.getInstance(androidContext()).noteDao }

        single<LocalLabelDataSource> { NotoDatabase.getInstance(androidContext()).labelDao }

        single<LocalNoteLabelDataSource> { NotoDatabase.getInstance(androidContext()).noteLabelDao }

        single<DataStore<Preferences>> { androidContext().dataStore }

    }

    @OptIn(SupabaseInternal::class)
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
                install(GoTrue) {
                    scheme = URLProtocol.HTTPS.name
                    host = SupabaseConstants.URLs.NotoHost
                }
                install(Postgrest)
            }
        }

        single<RemoteAuthDataSource> { SupabaseAuthClient(get()) }

        single<RemoteUserDataSource> { SupabaseUserClient(get()) }

        single<DeepLinksHandler> { SupabaseDeepLinksHandler(get()) }

        single<RemoteFolderDataSource> { SupabaseFolderClient(get()) }

        single<RemoteNoteDataSource> { SupabaseNoteClient(get()) }

        single<RemoteLabelDataSource> { SupabaseLabelClient(get()) }

    }

    val Crypto = module {

        single<PasswordTransformer> { PasswordTransformerImpl() }

        single<CryptoManager> { TinkCryptoManager() }

        single<Json>(Qualifiers.CryptoJson) { JsonConfigs.Crypto }

        single<KeyStoreManager> {
            val keyStore = KeyStore.getInstance(AndroidKeyStore).apply { load(null) }
            AndroidKeyStoreManager(keyStore, androidContext().dataStore)
        }

        single { EncryptionHandler(get(), get(Qualifiers.CryptoJson)) }

    }

    val RemoteService = module {

        single<RemoteFolderService> { AndroidRemoteFolderService(androidContext().applicationContext) }

        single<RemoteNoteService> { AndroidRemoteNoteService(androidContext().applicationContext) }

        single<RemoteLabelService> { AndroidRemoteLabelService(androidContext().applicationContext) }

    }

    val LocalBackup = module {

        single<LocalBackupHandler> { AndroidLocalBackupHandler(androidApplication(), get(), get(Qualifiers.CoroutineDispatcher)) }

    }

}