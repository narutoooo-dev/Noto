package com.noto.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.noto.app.AppViewModel
import com.noto.app.BuildConfig
import com.noto.app.crypto.PasswordTransformer
import com.noto.app.crypto.PasswordTransformerImpl
import com.noto.app.data.database.NotoDatabase
import com.noto.app.data.repository.*
import com.noto.app.data.source.remote.RemoteAuthClient
import com.noto.app.data.source.remote.RemoteUserClient
import com.noto.app.domain.repository.*
import com.noto.app.domain.source.LocalFolderDataSource
import com.noto.app.domain.source.LocalLabelDataSource
import com.noto.app.domain.source.LocalNoteDataSource
import com.noto.app.domain.source.LocalNoteLabelDataSource
import com.noto.app.domain.source.remote.RemoteAuthDataSource
import com.noto.app.domain.source.remote.RemoteUserDataSource
import com.noto.app.filtered.FilteredViewModel
import com.noto.app.folder.FolderViewModel
import com.noto.app.label.LabelViewModel
import com.noto.app.login.LoginViewModel
import com.noto.app.main.MainViewModel
import com.noto.app.note.NotePagerViewModel
import com.noto.app.note.NoteViewModel
import com.noto.app.settings.SettingsViewModel
import com.noto.app.widget.FolderListWidgetConfigViewModel
import com.noto.app.widget.NoteListWidgetConfigViewModel
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

private const val DataStoreName = "Noto Data Store"
private val Context.dataStore by preferencesDataStore(name = DataStoreName)
private val AuthClientQualifier = qualifier("AuthClient")
private val ClientQualifier = qualifier("Client")

val appModule = module {

    viewModel { MainViewModel(get(), get(), get()) }

    viewModel { FolderViewModel(get(), get(), get(), get(), get(), it.get(), it.getOrNull() ?: longArrayOf()) }

    viewModel { NoteViewModel(get(), get(), get(), get(), get(), it[0], it[1], it.getOrNull(), it.getOrNull() ?: longArrayOf()) }

    viewModel { AppViewModel(get(), get(), get(), get()) }

    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }

    viewModel { LabelViewModel(get(), get(), it[0], it[1]) }

    viewModel { FolderListWidgetConfigViewModel(it.get(), get(), get(), get()) }

    viewModel { NoteListWidgetConfigViewModel(it.get(), get(), get(), get(), get(), get()) }

    viewModel { NotePagerViewModel(get(), get(),get(), it[0], it[1], it[2], it[3]) }

    viewModel { FilteredViewModel(get(), get(), get(), get(), get(), it.get()) }

    viewModel { LoginViewModel(get(), get()) }

}

val repositoryModule = module {

    single<FolderRepository> { FolderRepositoryImpl(get()) }

    single<NoteRepository> { NoteRepositoryImpl(get()) }

    single<LabelRepository> { LabelRepositoryImpl(get()) }

    single<NoteLabelRepository> { NoteLabelRepositoryImpl(get()) }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    single<UserRepository> { UserRepositoryImpl(get(), get(), get(), passwordTransformer = get()) }

}

val localDataSourceModule = module {

    single<LocalFolderDataSource> { NotoDatabase.getInstance(androidContext()).folderDao }

    single<LocalNoteDataSource> { NotoDatabase.getInstance(androidContext()).noteDao }

    single<LocalLabelDataSource> { NotoDatabase.getInstance(androidContext()).labelDao }

    single<LocalNoteLabelDataSource> { NotoDatabase.getInstance(androidContext()).noteLabelDao }

    single<DataStore<Preferences>> { androidContext().dataStore }

}

val remoteDataSourceModule = module {

    single(AuthClientQualifier) { DefaultHttpClient() }

    single(ClientQualifier) {
        val settingsRepository by inject<SettingsRepository>()
        val authDataSource by inject<RemoteAuthDataSource>()
        DefaultHttpClient {
            Auth {
                bearer {
                    sendWithoutRequest { true }
                    loadTokens {
                        val accessToken = settingsRepository.accessToken.firstOrNull()
                        val refreshToken = settingsRepository.refreshToken.firstOrNull()
                        if (accessToken != null && refreshToken != null)
                            BearerTokens(accessToken, refreshToken)
                        else
                            null
                    }
                    refreshTokens {
                        val refreshToken = settingsRepository.refreshToken.firstOrNull()
                        if (refreshToken != null) {
                            val response = authDataSource.refreshToken(refreshToken)
                            settingsRepository.updateAccessToken(response.accessToken)
                            settingsRepository.updateRefreshToken(response.refreshToken)
                            BearerTokens(response.accessToken, response.refreshToken)
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }

    single<RemoteAuthDataSource> { RemoteAuthClient(get(AuthClientQualifier), get(ClientQualifier)) }

    single<RemoteUserDataSource> { RemoteUserClient(get(ClientQualifier)) }
}

val cryptoModule = module {

    single<PasswordTransformer> { PasswordTransformerImpl() }

}

private fun DefaultHttpClient(block: HttpClientConfig<CIOEngineConfig>.() -> Unit = {}) = HttpClient(CIO) {
    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "bcehffsgkofhyjoktqpe.supabase.co"
        }
        header(Constants.ApiKey, BuildConfig.SupabaseApiKey)
        contentType(ContentType.Application.Json)
    }
    install(ContentNegotiation) {
        json(NotoDefaultJson)
    }
    if (BuildConfig.DEBUG) {
        Logging {
            level = LogLevel.ALL
            logger = Logger.SIMPLE
        }
    }
    block()
}