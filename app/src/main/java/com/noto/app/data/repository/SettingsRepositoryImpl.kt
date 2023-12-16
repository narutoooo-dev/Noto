package com.noto.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.noto.app.data.model.local.LocalNotoData
import com.noto.app.data.model.local.LocalSettingsConfig
import com.noto.app.domain.model.*
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import com.noto.app.filtered.FilteredItemModel
import com.noto.app.util.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepositoryImpl(
    private val storage: DataStore<Preferences>,
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val jsonConverter: Json,
    private val coroutineDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    override val theme: Flow<Theme> = storage[SettingsKeys.Theme, Theme.System]

    override val font: Flow<Font> = storage[SettingsKeys.Font, Font.Nunito]

    override val language: Flow<Language> = storage[SettingsKeys.Language, Language.System]

    override val icon: Flow<Icon> = storage[SettingsKeys.Icon, Icon.Futuristic]

    override val vaultPasscode: Flow<String?> = storage.getNullable(SettingsKeys.VaultPasscode, null)

    override val vaultTimeout: Flow<VaultTimeout> = storage[SettingsKeys.VaultTimeout, VaultTimeout.Immediately]

    override val scheduledVaultTimeout: Flow<VaultTimeout?> = storage.data
        .map { preferences -> preferences[SettingsKeys.ScheduledVaultTimeout] }
        .map { if (it != null) VaultTimeout.valueOf(it) else null }
        .flowOn(coroutineDispatcher)

    override val isVaultOpen: Flow<Boolean> = storage.getNullable(SettingsKeys.IsVaultOpen, null).map { it.toBoolean() }

    override val isBioAuthEnabled: Flow<Boolean> = storage.getNullable(SettingsKeys.IsBioAuthEnabled, null).map { it.toBoolean() }

    override val isDoNotDisturb: Flow<Boolean> = storage[SettingsKeys.IsDoNotDisturb, false]

    override val isScreenOn: Flow<Boolean> = storage[SettingsKeys.IsScreenOn, true]

    override val isFullScreen: Flow<Boolean> = storage[SettingsKeys.IsFullScreen, true]

    override val lastVersion: Flow<String> = storage[SettingsKeys.LastVersion, Release.Version.Last.format()]

    override val sortingType: Flow<FolderListSortingType> = storage[SettingsKeys.FolderListSortingType, FolderListSortingType.CreationDate]

    override val sortingOrder: Flow<SortingOrder> = storage[SettingsKeys.FolderListSortingOrder, SortingOrder.Descending]

    override val isShowNotesCount: Flow<Boolean> = storage.getNullable(SettingsKeys.ShowNotesCount, null).map { it?.toBoolean() ?: true }

    override val mainInterfaceId: Flow<Long> = storage[SettingsKeys.MainInterfaceId, Constants.AllFoldersId]

    override val isRememberScrollingPosition: Flow<Boolean> = storage[SettingsKeys.IsRememberScrollingPosition, true]

    override val quickNoteFolderId: Flow<Long> = storage[SettingsKeys.QuickNoteFolderId, Folder.GeneralFolderId]

    override val screenBrightnessLevel: Flow<ScreenBrightnessLevel> = storage.data
        .map { preferences -> preferences[SettingsKeys.ScreenBrightnessLevel] }
        .map { ScreenBrightnessLevel.entries.firstOrNull { level -> level.value == it } ?: ScreenBrightnessLevel.System }
        .flowOn(coroutineDispatcher)

    override val quickExit: Flow<Boolean> = storage[SettingsKeys.QuickExit, false]

    override val continuousSearch: Flow<Boolean> = storage[SettingsKeys.ContinuousSearch, true]

    override val previewAutoScroll: Flow<Boolean> = storage[SettingsKeys.PreviewAutoScroll, true]

    override val id: Flow<String?> = storage.getNullable(SettingsKeys.Id, null)

    override val name: Flow<String?> = storage.getNullable(SettingsKeys.Name, null)

    override val email: Flow<String?> = storage.getNullable(SettingsKeys.Email, null)

    override val userStatus: Flow<UserStatus> = storage[SettingsKeys.UserStatus, UserStatus.New]

    override val isUserLoggedIn: Flow<Boolean> = userStatus.map { it == UserStatus.LoggedIn }

    override val autoBackupDuration: Flow<AutoBackupDuration> = storage[SettingsKeys.AutoBackupDuration, AutoBackupDuration.Daily]

    override val scheduledAutoBackupDuration: Flow<AutoBackupDuration?> = storage.data
        .map { preferences -> preferences[SettingsKeys.ScheduledAutoBackupDuration] }
        .map { if (it != null) AutoBackupDuration.valueOf(it) else null }
        .flowOn(coroutineDispatcher)

    override val autoBackupLocation: Flow<String?> = storage.getNullable(SettingsKeys.AutoBackupLocation, null)

    override fun getFilteredNotesScrollingPosition(model: FilteredItemModel): Flow<Int> = storage[SettingsKeys.FilteredItemModel(model), 0]

    override fun getWidgetFolderId(widgetId: Int): Flow<Long> = storage[SettingsKeys.Widget.FolderId(widgetId), 0L]

    override fun getIsWidgetCreated(widgetId: Int): Flow<Boolean> = storage.getNullable(SettingsKeys.Widget.Id(widgetId), null).map { it.toBoolean() }

    override fun getIsWidgetHeaderEnabled(widgetId: Int): Flow<Boolean> = storage.getNullable(SettingsKeys.Widget.Header(widgetId), null)
        .map { it?.toBoolean() ?: true }

    override fun getIsWidgetEditButtonEnabled(widgetId: Int): Flow<Boolean> = storage.getNullable(SettingsKeys.Widget.EditButton(widgetId), null)
        .map { it?.toBoolean() ?: true }

    override fun getIsWidgetAppIconEnabled(widgetId: Int): Flow<Boolean> = storage.getNullable(SettingsKeys.Widget.AppIcon(widgetId), null)
        .map { it?.toBoolean() ?: true }

    override fun getIsWidgetNewItemButtonEnabled(widgetId: Int): Flow<Boolean> = storage.getNullable(
        SettingsKeys.Widget.NewItemButton(widgetId),
        null
    )
        .map { it?.toBoolean() ?: true }

    override fun getWidgetNotesCount(widgetId: Int): Flow<Boolean> = storage.getNullable(SettingsKeys.Widget.NotesCount(widgetId), null)
        .map { it?.toBoolean() ?: true }

    override fun getWidgetRadius(widgetId: Int): Flow<Int> = storage.getNullable(SettingsKeys.Widget.Radius(widgetId), null)
        .map { it?.toIntOrNull() ?: 16 }

    override fun getWidgetSelectedLabelIds(widgetId: Int, folderId: Long): Flow<List<Long>> =
        storage.getNullable(SettingsKeys.Widget.SelectedLabelIds(widgetId, folderId), null)
            .map { it?.toLongList() ?: emptyList() }

    override fun getWidgetFilteringType(widgetId: Int): Flow<FilteringType> =
        storage[SettingsKeys.Widget.FilteringType(widgetId), FilteringType.Inclusive]

    override suspend fun updateTheme(theme: Theme) = storage.set(SettingsKeys.Theme, theme.toString())

    override suspend fun updateFont(font: Font) = storage.set(SettingsKeys.Font, font.toString())

    override suspend fun updateLanguage(language: Language) = storage.set(SettingsKeys.Language, language.toString())

    override suspend fun updateIcon(icon: Icon) = storage.set(SettingsKeys.Icon, icon.toString())

    override suspend fun updateVaultPasscode(passcode: String?) = storage.set(SettingsKeys.VaultPasscode, passcode)

    override suspend fun updateVaultTimeout(timeout: VaultTimeout) = storage.set(SettingsKeys.VaultTimeout, timeout.toString())

    override suspend fun updateScheduledVaultTimeout(timeout: VaultTimeout?) = storage.set(SettingsKeys.ScheduledVaultTimeout, timeout?.toString())

    override suspend fun updateIsVaultOpen(isOpen: Boolean) = storage.set(SettingsKeys.IsVaultOpen, isOpen.toString())

    override suspend fun updateIsBioAuthEnabled(isEnabled: Boolean) = storage.set(SettingsKeys.IsBioAuthEnabled, isEnabled.toString())

    override suspend fun updateLastVersion(version: String) = storage.set(SettingsKeys.LastVersion, version)

    override suspend fun updateSortingType(sortingType: FolderListSortingType) =
        storage.set(SettingsKeys.FolderListSortingType, sortingType.toString())

    override suspend fun updateSortingOrder(sortingOrder: SortingOrder) = storage.set(SettingsKeys.FolderListSortingOrder, sortingOrder.toString())

    override suspend fun updateIsShowNotesCount(isShow: Boolean) = storage.set(SettingsKeys.ShowNotesCount, isShow.toString())

    override suspend fun updateIsDoNotDisturb(isDoNotDisturb: Boolean) = storage.set(SettingsKeys.IsDoNotDisturb, isDoNotDisturb)

    override suspend fun updateIsScreenOn(isScreenOn: Boolean) = storage.set(SettingsKeys.IsScreenOn, isScreenOn)

    override suspend fun updateIsFullScreen(isFullScreen: Boolean) = storage.set(SettingsKeys.IsFullScreen, isFullScreen)

    override suspend fun updateMainInterfaceId(interfaceId: Long) = storage.set(SettingsKeys.MainInterfaceId, interfaceId)

    override suspend fun updateFilteredNotesScrollingPosition(model: FilteredItemModel, scrollingPosition: Int) =
        storage.set(SettingsKeys.FilteredItemModel(model), scrollingPosition)

    override suspend fun updateIsRememberScrollingPosition(isRememberScrollingPosition: Boolean) =
        storage.set(SettingsKeys.IsRememberScrollingPosition, isRememberScrollingPosition)

    override suspend fun updateWidgetFolderId(widgetId: Int, folderId: Long) = storage.set(SettingsKeys.Widget.FolderId(widgetId), folderId)

    override suspend fun updateIsWidgetCreated(widgetId: Int, isCreated: Boolean) =
        storage.set(SettingsKeys.Widget.Id(widgetId), isCreated.toString())

    override suspend fun updateIsWidgetHeaderEnabled(widgetId: Int, isEnabled: Boolean) =
        storage.set(SettingsKeys.Widget.Header(widgetId), isEnabled.toString())

    override suspend fun updateIsWidgetEditButtonEnabled(widgetId: Int, isEnabled: Boolean) =
        storage.set(SettingsKeys.Widget.EditButton(widgetId), isEnabled.toString())

    override suspend fun updateIsWidgetAppIconEnabled(widgetId: Int, isEnabled: Boolean) =
        storage.set(SettingsKeys.Widget.AppIcon(widgetId), isEnabled.toString())

    override suspend fun updateIsWidgetNewItemButtonEnabled(widgetId: Int, isEnabled: Boolean) =
        storage.set(SettingsKeys.Widget.NewItemButton(widgetId), isEnabled.toString())

    override suspend fun updateWidgetNotesCount(widgetId: Int, isEnabled: Boolean) =
        storage.set(SettingsKeys.Widget.NotesCount(widgetId), isEnabled.toString())

    override suspend fun updateWidgetRadius(widgetId: Int, radius: Int) = storage.set(SettingsKeys.Widget.Radius(widgetId), radius.toString())

    override suspend fun updateWidgetSelectedLabelIds(widgetId: Int, folderId: Long, labelIds: List<Long>) =
        storage.set(SettingsKeys.Widget.SelectedLabelIds(widgetId, folderId), labelIds.joinToString())

    override suspend fun updateWidgetFilteringType(widgetId: Int, filteringType: FilteringType) =
        storage.set(SettingsKeys.Widget.FilteringType(widgetId), filteringType.toString())

    override suspend fun updateQuickNoteFolderId(folderId: Long) = storage.set(SettingsKeys.QuickNoteFolderId, folderId)

    override suspend fun updateScreenBrightnessLevel(level: ScreenBrightnessLevel) = storage.set(SettingsKeys.ScreenBrightnessLevel, level.value)

    override suspend fun updateQuickExit(enabled: Boolean) = storage.set(SettingsKeys.QuickExit, enabled)

    override suspend fun updateContinuousSearch(isEnabled: Boolean) = storage.set(SettingsKeys.ContinuousSearch, isEnabled)

    override suspend fun updatePreviewAutoScroll(isEnabled: Boolean) = storage.set(SettingsKeys.PreviewAutoScroll, isEnabled)

    override suspend fun updateId(id: String) = storage.set(SettingsKeys.Id, id)

    override suspend fun updateName(name: String) = storage.set(SettingsKeys.Name, name)

    override suspend fun updateEmail(email: String) = storage.set(SettingsKeys.Email, email)

    override suspend fun updateUserStatus(userStatus: UserStatus) = storage.set(SettingsKeys.UserStatus, userStatus.toString())

    override suspend fun updateAutoBackupDuration(autoBackupDuration: AutoBackupDuration) =
        storage.set(SettingsKeys.AutoBackupDuration, autoBackupDuration.toString())

    override suspend fun updateScheduledAutoBackupDuration(autoBackupDuration: AutoBackupDuration?) =
        storage.set(SettingsKeys.ScheduledAutoBackupDuration, autoBackupDuration?.toString())

    override suspend fun updateAutoBackupLocation(autoBackupLocation: String?) = storage.set(SettingsKeys.AutoBackupLocation, autoBackupLocation)

    override suspend fun exportNotoData(): String {
        return withContext(coroutineDispatcher) {
            val folders = localFolderDataSource.getAllLocalFolders().first()
            val notes = localNoteDataSource.getAllLocalNotes().first()
            val labels = localLabelDataSource.getAllLabels().first()
            val noteLabels = localNoteLabelDataSource.getAllNoteLabels().first()
            val config = config.first()
            val data = LocalNotoData(folders, notes, labels, noteLabels, config)
            jsonConverter.encodeToString(data)
        }
    }

    override suspend fun importNotoData(data: String) {
        withContext(coroutineDispatcher) {
            val folderIds = mutableMapOf<Long, Long>()
            val noteIds = mutableMapOf<Long, Long>()
            val labelIds = mutableMapOf<Long, Long>()
            val data = jsonConverter.decodeFromString<LocalNotoData>(data)
            data.apply {
                folders.forEach { localFolder ->
                    if (localFolder.id == Folder.GeneralFolderId) {
                        localFolderDataSource.updateLocalFolder(localFolder)
                        folderIds[localFolder.id] = Folder.GeneralFolderId
                    } else {
                        val parentFolder = folders.firstOrNull { it.id == localFolder.parentId }
                        val parentId = folderIds.getOrDefault(parentFolder?.id ?: 0L, 0L).takeUnless { it == 0L }
                        val newFolderId = localFolderDataSource.createLocalFolder(localFolder.copy(id = 0, parentId = parentId))
                        folderIds[localFolder.id] = newFolderId
                    }
                }
                notes.forEach { localNote ->
                    val folderId = folderIds.getValue(localNote.folderId)
                    val newNoteId = localNoteDataSource.createLocalNote(localNote.copy(id = 0, folderId = folderId))
                    noteIds[localNote.id] = newNoteId
                }
                labels.forEach { localLabel ->
                    val folderId = folderIds.getValue(localLabel.folderId)
                    val newLabelId = localLabelDataSource.createLabel(localLabel.copy(id = 0, folderId = folderId))
                    labelIds[localLabel.id] = newLabelId
                }
                noteLabels.forEach { noteLabel ->
                    val noteId = noteIds.getValue(noteLabel.noteId)
                    val labelId = labelIds.getValue(noteLabel.labelId)
                    localNoteLabelDataSource.createNoteLabel(noteLabel.copy(id = 0, noteId = noteId, labelId = labelId))
                }
                updateConfig(settings)
            }
        }
    }

    private val config: Flow<LocalSettingsConfig> = storage.data.map {
        LocalSettingsConfig(
            theme.first(),
            font.first(),
            language.first(),
            icon.first(),
            vaultPasscode.first(),
            vaultTimeout.first(),
            scheduledVaultTimeout.first(),
            isVaultOpen.first(),
            isBioAuthEnabled.first(),
            lastVersion.first(),
            sortingType.first(),
            sortingOrder.first(),
            isShowNotesCount.first(),
            isDoNotDisturb.first(),
            isScreenOn.first(),
            mainInterfaceId.first(),
            isRememberScrollingPosition.first(),
            getFilteredNotesScrollingPosition(FilteredItemModel.All).first(),
            getFilteredNotesScrollingPosition(FilteredItemModel.Recent).first(),
            getFilteredNotesScrollingPosition(FilteredItemModel.Scheduled).first(),
            getFilteredNotesScrollingPosition(FilteredItemModel.Archived).first(),
        )
    }.flowOn(coroutineDispatcher)

    private suspend fun updateConfig(config: LocalSettingsConfig) {
        withContext(coroutineDispatcher) {
            with(config) {
                updateTheme(theme)
                updateFont(font)
                updateLanguage(language)
                updateIcon(icon)
                if (vaultPasscode != null) updateVaultPasscode(vaultPasscode)
                updateVaultTimeout(vaultTimeout)
                updateScheduledVaultTimeout(scheduledVaultTimeout)
                updateIsVaultOpen(isVaultOpen)
                updateIsBioAuthEnabled(isBioAuthEnabled)
                updateLastVersion(lastVersion)
                updateSortingType(sortingType)
                updateSortingOrder(sortingOrder)
                updateIsShowNotesCount(isShowNotesCount)
                updateIsDoNotDisturb(isDoNotDisturb)
                updateIsScreenOn(isScreenOn)
                updateMainInterfaceId(mainInterfaceId)
                updateIsRememberScrollingPosition(isRememberScrollingPosition)
                updateFilteredNotesScrollingPosition(FilteredItemModel.All, allNotesScrollingPosition)
                updateFilteredNotesScrollingPosition(FilteredItemModel.Recent, recentNotesScrollingPosition)
                updateFilteredNotesScrollingPosition(FilteredItemModel.Scheduled, scheduledNotesScrollingPosition)
                updateFilteredNotesScrollingPosition(FilteredItemModel.Archived, archivedNotesScrollingPosition)
            }
        }
    }

    override suspend fun clearSettings() {
        withContext(coroutineDispatcher) {
            storage.edit { it.clear() }
        }
    }

    private fun String.toLongList() = split(", ").mapNotNull { it.toLongOrNull() }

    private operator fun <T> DataStore<Preferences>.get(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return this.data
            .map { preferences -> preferences[key] ?: defaultValue }
            .flowOn(coroutineDispatcher)
    }

    private fun <T> DataStore<Preferences>.getNullable(key: Preferences.Key<T>, defaultValue: T?): Flow<T?> {
        return this.data
            .map { preferences -> preferences[key] ?: defaultValue }
            .flowOn(coroutineDispatcher)
    }

    private inline operator fun <reified E : Enum<E>> DataStore<Preferences>.get(key: Preferences.Key<String>, defaultValue: E): Flow<E> {
        return this.data
            .map { preferences -> preferences[key] }
            .map { if (it != null) enumValueOf(it) else defaultValue }
            .flowOn(coroutineDispatcher)
    }

    private suspend fun <T> DataStore<Preferences>.set(key: Preferences.Key<T>, value: T?) {
        withContext(coroutineDispatcher) {
            edit { preferences ->
                if (value != null)
                    preferences[key] = value
                else
                    preferences.remove(key)
            }
        }
    }

}