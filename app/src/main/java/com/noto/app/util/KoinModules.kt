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
import com.noto.app.data.service.AndroidRemoteNoteService
import com.noto.app.data.source.remote.*
import com.noto.app.domain.model.DeepLinksHandler
import com.noto.app.domain.repository.*
import com.noto.app.domain.service.RemoteFolderService
import com.noto.app.domain.service.RemoteNoteService
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import com.noto.app.domain.source.remote.RemoteAuthDataSource
import com.noto.app.domain.source.remote.RemoteFolderDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import com.noto.app.domain.source.remote.RemoteUserDataSource
import com.noto.app.filtered.FilteredViewModel
import com.noto.app.folder.FolderViewModel
import com.noto.app.folder.NewFolderViewModel
import com.noto.app.label.LabelViewModel
import com.noto.app.label.NewLabelViewModel
import com.noto.app.login.CreateAccountViewModel
import com.noto.app.login.LoginViewModel
import com.noto.app.login.VerifyEmailViewModel
import com.noto.app.main.MainViewModel
import com.noto.app.note.NotePagerViewModel
import com.noto.app.note.NoteViewModel
import com.noto.app.settings.SettingsViewModel
import com.noto.app.settings.account.AccountSettingsViewModel
import com.noto.app.settings.readingmode.ReadingModeSettingsViewModel
import com.noto.app.settings.vault.VaultSettingsViewModel
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

private const val DataStoreName = "Noto Data Store"
private val Context.dataStore by preferencesDataStore(name = DataStoreName)
val CoroutineDispatcherQualifier = qualifier("CoroutineDispatcher")
val CryptoJsonQualifier = qualifier("CryptoJson")
private const val AndroidKeyStore = "AndroidKeyStore"

val appModule = module {

    viewModel { MainViewModel(get(), get(), get()) }

    viewModel { FolderViewModel(get(), get(), get(), get(), it.get(), it.getOrNull() ?: longArrayOf()) }

    viewModel { NoteViewModel(get(), get(), get(), get(), it[0], it[1], it.getOrNull(), it.getOrNull() ?: longArrayOf()) }

    viewModel { AppViewModel(get(), get(), get()) }

    viewModel { SettingsViewModel(get(), get(), androidApplication()) }

    viewModel { LabelViewModel(get(), get(), it[0], it[1]) }

    viewModel { FolderListWidgetConfigViewModel(it.get(), get(), get()) }

    viewModel { NoteListWidgetConfigViewModel(it.get(), get(), get(), get(), get()) }

    viewModel { NotePagerViewModel(get(), get(), get(), it[0], it[1], it[2], it[3]) }

    viewModel { FilteredViewModel(get(), get(), get(), it.get()) }

    viewModel { LoginViewModel(get(), get()) }

    viewModel { CreateAccountViewModel(get()) }

    viewModel { VerifyEmailViewModel(get(), get(), it.get()) }

    viewModel { NewFolderViewModel(get(), get()) }

    viewModel { NewLabelViewModel(get(), get(), it[0], it[1]) }

    viewModel { VaultSettingsViewModel(get(), get()) }

    viewModel { VaultPasscodeViewModel(get()) }

    viewModel { AccountSettingsViewModel(get(), get(), get(), get(), get(), get()) }

    viewModel { ReadingModeSettingsViewModel(get()) }

}

val repositoryModule = module {

    single<FolderRepository> { FolderRepositoryImpl(get(), get(), get(), get(), get(), get(), get(CoroutineDispatcherQualifier)) }

    single<NoteRepository> { NoteRepositoryImpl(get(), get(), get(), get(), get(), get(), get(), get(CoroutineDispatcherQualifier)) }

    single<LabelRepository> { LabelRepositoryImpl(get()) }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            JsonConfigs.ExportImportData,
            get(CoroutineDispatcherQualifier)
        )
    }

    single<UserRepository> { UserRepositoryImpl(get(), get(), get(), passwordTransformer = get(), keyStoreManager = get()) }

    single<CoroutineDispatcher>(CoroutineDispatcherQualifier) { Dispatchers.IO }

    single<LocalGeneralFolderManager> { FolderRepositoryImpl(get(), get(), get(), get(), get(), get(), get(CoroutineDispatcherQualifier)) }

}

val localDataSourceModule = module {

    single<LocalFolderDataSource> { NotoDatabase.getInstance(androidContext()).folderDao }

    single<LocalNoteDataSource> { NotoDatabase.getInstance(androidContext()).noteDao }

    single<LocalLabelDataSource> { NotoDatabase.getInstance(androidContext()).labelDao }

    single<LocalNoteLabelDataSource> { NotoDatabase.getInstance(androidContext()).noteLabelDao }

    single<DataStore<Preferences>> { androidContext().dataStore }

}

@OptIn(SupabaseInternal::class)
val remoteDataSourceModule = module {

    single<SupabaseClient> {
        createSupabaseClient(SupabaseConstants.URLs.SupabaseUrl, BuildConfig.SupabaseApiKey) {
            defaultSerializer = KotlinXSerializer(JsonConfigs.Remote)
            httpConfig {
                if (BuildConfig.DEBUG) {
                    Logging {
                        level = LogLevel.ALL
                        logger = Logger.SIMPLE
                    }
                }
            }
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

}

val cryptoModule = module {

    single<PasswordTransformer> { PasswordTransformerImpl() }

    single<EncryptionHandler> { TinkEncryptionHandler() }

    single<Json>(CryptoJsonQualifier) { JsonConfigs.Crypto }

    single<KeyStoreManager> {
        val keyStore = KeyStore.getInstance(AndroidKeyStore).apply { load(null) }
        AndroidKeyStoreManager(keyStore, androidContext().dataStore)
    }

}

val remoteServiceModule = module {

    single<RemoteFolderService> { AndroidRemoteFolderService(androidContext().applicationContext) }

    single<RemoteNoteService> { AndroidRemoteNoteService(androidContext().applicationContext) }

}