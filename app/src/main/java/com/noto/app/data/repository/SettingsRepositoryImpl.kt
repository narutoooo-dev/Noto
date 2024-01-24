package com.noto.app.data.repository

import com.noto.app.crypto.KeyStoreManager
import com.noto.app.crypto.RawAesCryptoManager
import com.noto.app.crypto.key.Argon2KeyGenerator
import com.noto.app.data.model.local.LocalNotoData
import com.noto.app.data.model.local.LocalSettingsConfig
import com.noto.app.data.source.local.LocalSettingsDataSource
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
    private val localSettingsDataSource: LocalSettingsDataSource,
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val keyStoreManager: KeyStoreManager,
    private val keyGenerator: Argon2KeyGenerator,
    private val cryptoManager: RawAesCryptoManager,
    private val jsonConverter: Json,
    private val coroutineDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    override val theme: Flow<Theme> = localSettingsDataSource.getEnumOrDefault(SettingsKeys.Theme, Theme.System)

    override val font: Flow<Font> = localSettingsDataSource.getEnumOrDefault(SettingsKeys.Font, Font.Nunito)

    override val language: Flow<Language> = localSettingsDataSource.getEnumOrDefault(SettingsKeys.Language, Language.System)

    override val icon: Flow<Icon> = localSettingsDataSource.getEnumOrDefault(SettingsKeys.Icon, Icon.Futuristic)

    override val vaultPasscode: Flow<String?> = localSettingsDataSource.getOrNull(SettingsKeys.VaultPasscode)

    override val vaultTimeout: Flow<VaultTimeout> = localSettingsDataSource.getEnumOrDefault(SettingsKeys.VaultTimeout, VaultTimeout.Immediately)

    override val scheduledVaultTimeout: Flow<VaultTimeout?> = localSettingsDataSource.getEnumOrNull(SettingsKeys.ScheduledVaultTimeout)

    override val isVaultOpen: Flow<Boolean> = localSettingsDataSource.getOrNull(SettingsKeys.IsVaultOpen).map { it.toBoolean() }

    override val isBioAuthEnabled: Flow<Boolean> = localSettingsDataSource.getOrNull(SettingsKeys.IsBioAuthEnabled).map { it.toBoolean() }

    override val isDoNotDisturb: Flow<Boolean> = localSettingsDataSource.getOrDefault(SettingsKeys.IsDoNotDisturb, false)

    override val isScreenOn: Flow<Boolean> = localSettingsDataSource.getOrDefault(SettingsKeys.IsScreenOn, true)

    override val isFullScreen: Flow<Boolean> = localSettingsDataSource.getOrDefault(SettingsKeys.IsFullScreen, true)

    override val lastVersion: Flow<String> = localSettingsDataSource.getOrDefault(SettingsKeys.LastVersion, Release.Version.Last.format())

    override val sortingType: Flow<FolderListSortingType> =
        localSettingsDataSource.getEnumOrDefault(SettingsKeys.FolderListSortingType, FolderListSortingType.CreationDate)

    override val sortingOrder: Flow<SortingOrder> =
        localSettingsDataSource.getEnumOrDefault(SettingsKeys.FolderListSortingOrder, SortingOrder.Descending)

    override val isShowNotesCount: Flow<Boolean> =
        localSettingsDataSource.getOrNull(SettingsKeys.ShowNotesCount).map { it?.toBoolean() ?: true }

    override val mainInterfaceId: Flow<Long> = localSettingsDataSource.getOrDefault(SettingsKeys.MainInterfaceId, Constants.AllFoldersId)

    override val isRememberScrollingPosition: Flow<Boolean> = localSettingsDataSource.getOrDefault(SettingsKeys.IsRememberScrollingPosition, true)

    override val quickNoteFolderId: Flow<Long> = localSettingsDataSource.getOrDefault(SettingsKeys.QuickNoteFolderId, Folder.GeneralFolderId)

    override val screenBrightnessLevel: Flow<ScreenBrightnessLevel> =
        localSettingsDataSource.getOrDefault(SettingsKeys.ScreenBrightnessLevel, ScreenBrightnessLevel.System.value)
            .map { ScreenBrightnessLevel.entries.firstOrNull { level -> level.value == it } ?: ScreenBrightnessLevel.System }
            .flowOn(coroutineDispatcher)

    override val quickExit: Flow<Boolean> = localSettingsDataSource.getOrDefault(SettingsKeys.QuickExit, false)

    override val continuousSearch: Flow<Boolean> = localSettingsDataSource.getOrDefault(SettingsKeys.ContinuousSearch, true)

    override val previewAutoScroll: Flow<Boolean> = localSettingsDataSource.getOrDefault(SettingsKeys.PreviewAutoScroll, true)

    override val id: Flow<String?> = localSettingsDataSource.getOrNull(SettingsKeys.Id)

    override val name: Flow<String?> = localSettingsDataSource.getOrNull(SettingsKeys.Name)

    override val email: Flow<String?> = localSettingsDataSource.getOrNull(SettingsKeys.Email)

    override val userStatus: Flow<UserStatus> = localSettingsDataSource.getEnumOrDefault(SettingsKeys.UserStatus, UserStatus.New)

    override val isUserLoggedIn: Flow<Boolean> = userStatus.map { it == UserStatus.LoggedIn }

    override val autoBackupDuration: Flow<AutoBackupDuration> =
        localSettingsDataSource.getEnumOrDefault(SettingsKeys.AutoBackupDuration, AutoBackupDuration.Daily)

    override val scheduledAutoBackupDuration: Flow<AutoBackupDuration?> =
        localSettingsDataSource.getEnumOrNull(SettingsKeys.ScheduledAutoBackupDuration)

    override val autoBackupFormat: Flow<BackupFormat> =
        localSettingsDataSource.getEnumOrDefault(SettingsKeys.AutoBackupFormat, BackupFormat.PlainText)

    override val keyEncryptionKeyParameters: Flow<String?> = localSettingsDataSource.getOrNull(SettingsKeys.KeyEncryptionKeyParameters)

    override val autoBackupLocation: Flow<String?> = localSettingsDataSource.getOrNull(SettingsKeys.AutoBackupLocation)

    override fun getFilteredNotesScrollingPosition(model: FilteredItemModel): Flow<Int> =
        localSettingsDataSource.getOrDefault(SettingsKeys.FilteredItemModel(model), 0)

    override fun getWidgetFolderId(widgetId: Int): Flow<Long> = localSettingsDataSource.getOrDefault(SettingsKeys.Widget.FolderId(widgetId), 0L)

    override fun getIsWidgetCreated(widgetId: Int): Flow<Boolean> =
        localSettingsDataSource.getOrNull(SettingsKeys.Widget.Id(widgetId)).map { it.toBoolean() }

    override fun getIsWidgetHeaderEnabled(widgetId: Int): Flow<Boolean> =
        localSettingsDataSource.getOrNull(SettingsKeys.Widget.Header(widgetId))
            .map { it?.toBoolean() ?: true }

    override fun getIsWidgetEditButtonEnabled(widgetId: Int): Flow<Boolean> =
        localSettingsDataSource.getOrNull(SettingsKeys.Widget.EditButton(widgetId))
            .map { it?.toBoolean() ?: true }

    override fun getIsWidgetAppIconEnabled(widgetId: Int): Flow<Boolean> =
        localSettingsDataSource.getOrNull(SettingsKeys.Widget.AppIcon(widgetId))
            .map { it?.toBoolean() ?: true }

    override fun getIsWidgetNewItemButtonEnabled(widgetId: Int): Flow<Boolean> =
        localSettingsDataSource.getOrNull(SettingsKeys.Widget.NewItemButton(widgetId))
            .map { it?.toBoolean() ?: true }

    override fun getWidgetNotesCount(widgetId: Int): Flow<Boolean> =
        localSettingsDataSource.getOrNull(SettingsKeys.Widget.NotesCount(widgetId))
            .map { it?.toBoolean() ?: true }

    override fun getWidgetRadius(widgetId: Int): Flow<Int> = localSettingsDataSource.getOrNull(SettingsKeys.Widget.Radius(widgetId))
        .map { it?.toIntOrNull() ?: 16 }

    override fun getWidgetSelectedLabelIds(widgetId: Int, folderId: Long): Flow<List<Long>> =
        localSettingsDataSource.getOrNull(SettingsKeys.Widget.SelectedLabelIds(widgetId, folderId))
            .map { it?.toLongList() ?: emptyList() }

    override fun getWidgetFilteringType(widgetId: Int): Flow<FilteringType> =
        localSettingsDataSource.getEnumOrDefault(SettingsKeys.Widget.FilteringType(widgetId), FilteringType.Inclusive)

    override suspend fun updateTheme(theme: Theme) = localSettingsDataSource.set(SettingsKeys.Theme, theme.toString())

    override suspend fun updateFont(font: Font) = localSettingsDataSource.set(SettingsKeys.Font, font.toString())

    override suspend fun updateLanguage(language: Language) = localSettingsDataSource.set(SettingsKeys.Language, language.toString())

    override suspend fun updateIcon(icon: Icon) = localSettingsDataSource.set(SettingsKeys.Icon, icon.toString())

    override suspend fun updateVaultPasscode(passcode: String?) = localSettingsDataSource.set(SettingsKeys.VaultPasscode, passcode)

    override suspend fun updateVaultTimeout(timeout: VaultTimeout) = localSettingsDataSource.set(SettingsKeys.VaultTimeout, timeout.toString())

    override suspend fun updateScheduledVaultTimeout(timeout: VaultTimeout?) =
        localSettingsDataSource.set(SettingsKeys.ScheduledVaultTimeout, timeout?.toString())

    override suspend fun updateIsVaultOpen(isOpen: Boolean) = localSettingsDataSource.set(SettingsKeys.IsVaultOpen, isOpen.toString())

    override suspend fun updateIsBioAuthEnabled(isEnabled: Boolean) = localSettingsDataSource.set(SettingsKeys.IsBioAuthEnabled, isEnabled.toString())

    override suspend fun updateLastVersion(version: String) = localSettingsDataSource.set(SettingsKeys.LastVersion, version)

    override suspend fun updateSortingType(sortingType: FolderListSortingType) =
        localSettingsDataSource.set(SettingsKeys.FolderListSortingType, sortingType.toString())

    override suspend fun updateSortingOrder(sortingOrder: SortingOrder) =
        localSettingsDataSource.set(SettingsKeys.FolderListSortingOrder, sortingOrder.toString())

    override suspend fun updateIsShowNotesCount(isShow: Boolean) = localSettingsDataSource.set(SettingsKeys.ShowNotesCount, isShow.toString())

    override suspend fun updateIsDoNotDisturb(isDoNotDisturb: Boolean) = localSettingsDataSource.set(SettingsKeys.IsDoNotDisturb, isDoNotDisturb)

    override suspend fun updateIsScreenOn(isScreenOn: Boolean) = localSettingsDataSource.set(SettingsKeys.IsScreenOn, isScreenOn)

    override suspend fun updateIsFullScreen(isFullScreen: Boolean) = localSettingsDataSource.set(SettingsKeys.IsFullScreen, isFullScreen)

    override suspend fun updateMainInterfaceId(interfaceId: Long) = localSettingsDataSource.set(SettingsKeys.MainInterfaceId, interfaceId)

    override suspend fun updateFilteredNotesScrollingPosition(model: FilteredItemModel, scrollingPosition: Int) =
        localSettingsDataSource.set(SettingsKeys.FilteredItemModel(model), scrollingPosition)

    override suspend fun updateIsRememberScrollingPosition(isRememberScrollingPosition: Boolean) =
        localSettingsDataSource.set(SettingsKeys.IsRememberScrollingPosition, isRememberScrollingPosition)

    override suspend fun updateWidgetFolderId(widgetId: Int, folderId: Long) =
        localSettingsDataSource.set(SettingsKeys.Widget.FolderId(widgetId), folderId)

    override suspend fun updateIsWidgetCreated(widgetId: Int, isCreated: Boolean) =
        localSettingsDataSource.set(SettingsKeys.Widget.Id(widgetId), isCreated.toString())

    override suspend fun updateIsWidgetHeaderEnabled(widgetId: Int, isEnabled: Boolean) =
        localSettingsDataSource.set(SettingsKeys.Widget.Header(widgetId), isEnabled.toString())

    override suspend fun updateIsWidgetEditButtonEnabled(widgetId: Int, isEnabled: Boolean) =
        localSettingsDataSource.set(SettingsKeys.Widget.EditButton(widgetId), isEnabled.toString())

    override suspend fun updateIsWidgetAppIconEnabled(widgetId: Int, isEnabled: Boolean) =
        localSettingsDataSource.set(SettingsKeys.Widget.AppIcon(widgetId), isEnabled.toString())

    override suspend fun updateIsWidgetNewItemButtonEnabled(widgetId: Int, isEnabled: Boolean) =
        localSettingsDataSource.set(SettingsKeys.Widget.NewItemButton(widgetId), isEnabled.toString())

    override suspend fun updateWidgetNotesCount(widgetId: Int, isEnabled: Boolean) =
        localSettingsDataSource.set(SettingsKeys.Widget.NotesCount(widgetId), isEnabled.toString())

    override suspend fun updateWidgetRadius(widgetId: Int, radius: Int) =
        localSettingsDataSource.set(SettingsKeys.Widget.Radius(widgetId), radius.toString())

    override suspend fun updateWidgetSelectedLabelIds(widgetId: Int, folderId: Long, labelIds: List<Long>) =
        localSettingsDataSource.set(SettingsKeys.Widget.SelectedLabelIds(widgetId, folderId), labelIds.joinToString())

    override suspend fun updateWidgetFilteringType(widgetId: Int, filteringType: FilteringType) =
        localSettingsDataSource.set(SettingsKeys.Widget.FilteringType(widgetId), filteringType.toString())

    override suspend fun updateQuickNoteFolderId(folderId: Long) = localSettingsDataSource.set(SettingsKeys.QuickNoteFolderId, folderId)

    override suspend fun updateScreenBrightnessLevel(level: ScreenBrightnessLevel) =
        localSettingsDataSource.set(SettingsKeys.ScreenBrightnessLevel, level.value)

    override suspend fun updateQuickExit(enabled: Boolean) = localSettingsDataSource.set(SettingsKeys.QuickExit, enabled)

    override suspend fun updateContinuousSearch(isEnabled: Boolean) = localSettingsDataSource.set(SettingsKeys.ContinuousSearch, isEnabled)

    override suspend fun updatePreviewAutoScroll(isEnabled: Boolean) = localSettingsDataSource.set(SettingsKeys.PreviewAutoScroll, isEnabled)

    override suspend fun updateId(id: String) = localSettingsDataSource.set(SettingsKeys.Id, id)

    override suspend fun updateName(name: String) = localSettingsDataSource.set(SettingsKeys.Name, name)

    override suspend fun updateEmail(email: String) = localSettingsDataSource.set(SettingsKeys.Email, email)

    override suspend fun updateUserStatus(userStatus: UserStatus) = localSettingsDataSource.set(SettingsKeys.UserStatus, userStatus.toString())

    override suspend fun updateAutoBackupDuration(autoBackupDuration: AutoBackupDuration) =
        localSettingsDataSource.set(SettingsKeys.AutoBackupDuration, autoBackupDuration.toString())

    override suspend fun updateScheduledAutoBackupDuration(autoBackupDuration: AutoBackupDuration?) =
        localSettingsDataSource.set(SettingsKeys.ScheduledAutoBackupDuration, autoBackupDuration?.toString())

    override suspend fun updateAutoBackupFormat(autoBackupFormat: BackupFormat) =
        localSettingsDataSource.set(SettingsKeys.AutoBackupFormat, autoBackupFormat.toString())

    override suspend fun updateKeyEncryptionKeyParameters(parameters: String) =
        localSettingsDataSource.set(SettingsKeys.KeyEncryptionKeyParameters, parameters)

    override suspend fun updateAutoBackupLocation(autoBackupLocation: String?) =
        localSettingsDataSource.set(SettingsKeys.AutoBackupLocation, autoBackupLocation)

    override suspend fun updateAutoBackupPasscode(passcode: String?) {
        if (passcode != null) {
            val keyData = keyGenerator.generateKey(passcode.encodeToByteArray())
            keyStoreManager.storeKey(KeyStoreManager.AutoBackupPasscodeId, keyData.key)
            localSettingsDataSource.set(SettingsKeys.AutoBackupEncryptionParameters, keyData.encodedParameters)
        } else {
            keyStoreManager.deleteKey(KeyStoreManager.AutoBackupPasscodeId)
            localSettingsDataSource.set(SettingsKeys.AutoBackupEncryptionParameters, null)
        }
    }

    override suspend fun exportNotoData(): Result<String> = runCatching {
        withContext(coroutineDispatcher) {
            val folders = localFolderDataSource.getAllLocalFolders().first()
            val notes = localNoteDataSource.getAllLocalNotes().first()
            val labels = localLabelDataSource.getAllLocalLabels().first()
            val noteLabels = localNoteLabelDataSource.getAllNoteLabels().first()
            val config = config.first()
            val data = LocalNotoData(folders, notes, labels, noteLabels, config)
            jsonConverter.encodeToString(data)
        }
    }

    override suspend fun exportEncryptedNotoData(): Result<String> = runCatching {
        withContext(coroutineDispatcher) {
            val key = keyStoreManager.getKey(KeyStoreManager.AutoBackupPasscodeId) ?: NotoException.LocalBackup.MissingPasscode()
            val parameters = localSettingsDataSource.getOrNull(SettingsKeys.AutoBackupEncryptionParameters)
                .first() ?: NotoException.LocalBackup.MissingPasscode()
            val encodedData = exportNotoData().getOrThrow()
            val encryptedContent = cryptoManager.encryptData(key, RawAesCryptoManager.FixedIv, encodedData.encodeToByteArray())
            val encryptedData = LocalNotoData.Encrypted(
                encryptedContent = encryptedContent,
                encodedParameters = parameters,
            )
            jsonConverter.encodeToString(encryptedData)
        }
    }

    override suspend fun exportEncryptedNotoData(passcode: String): Result<String> = runCatching {
        withContext(coroutineDispatcher) {
            val keyData = keyGenerator.generateKey(passcode.encodeToByteArray())
            val encodedData = exportNotoData().getOrThrow()
            val encryptedContent = cryptoManager.encryptData(keyData.key, RawAesCryptoManager.FixedIv, encodedData.encodeToByteArray())
            val encryptedData = LocalNotoData.Encrypted(
                encryptedContent = encryptedContent,
                encodedParameters = keyData.encodedParameters,
            )
            jsonConverter.encodeToString(encryptedData)
        }
    }

    override suspend fun importNotoData(data: String) = runCatching {
        withContext(coroutineDispatcher) {
            val folderIds = mutableMapOf<Long, Long>()
            val noteIds = mutableMapOf<Long, Long>()
            val labelIds = mutableMapOf<Long, Long>()
            jsonConverter.decodeFromString<LocalNotoData>(data).run {
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
                    val newLabelId = localLabelDataSource.createLocalLabel(localLabel.copy(id = 0, folderId = folderId))
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

    override suspend fun importEncryptedNotoData(data: String): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val key = keyStoreManager.getKey(KeyStoreManager.AutoBackupPasscodeId) ?: NotoException.LocalBackup.MissingPasscode()
            val encryptedData = jsonConverter.decodeFromString<LocalNotoData.Encrypted>(data)
            val decryptedContent = cryptoManager.decryptData(key, RawAesCryptoManager.FixedIv, encryptedData.encryptedContent)
            val decodedData = decryptedContent.decodeToString()
            importNotoData(decodedData).getOrThrow()
        }
    }

    override suspend fun importEncryptedNotoData(data: String, passcode: String): Result<Unit> = runCatching {
        withContext(coroutineDispatcher) {
            val encryptedData = jsonConverter.decodeFromString<LocalNotoData.Encrypted>(data)
            val keyData = keyGenerator.generateKey(passcode.encodeToByteArray(), encryptedData.encodedParameters)
            val decryptedContent = cryptoManager.decryptData(keyData.key, RawAesCryptoManager.FixedIv, encryptedData.encryptedContent)
            val decodedData = decryptedContent.decodeToString()
            importNotoData(decodedData).getOrThrow()
        }
    }

    private val config: Flow<LocalSettingsConfig> = localSettingsDataSource.storage.data.map {
        LocalSettingsConfig(
            theme = theme.first(),
            font = font.first(),
            language = language.first(),
            icon = icon.first(),
            vaultPasscode = vaultPasscode.first(),
            sortingType = sortingType.first(),
            sortingOrder = sortingOrder.first(),
            isShowNotesCount = isShowNotesCount.first(),
            isDoNotDisturb = isDoNotDisturb.first(),
            isScreenOn = isScreenOn.first(),
            isFullScreen = isFullScreen.first(),
            mainInterfaceId = mainInterfaceId.first(),
            quickNoteFolderId = quickNoteFolderId.first(),
            isRememberScrollingPosition = isRememberScrollingPosition.first(),
            screenBrightnessLevel = screenBrightnessLevel.first(),
            quickExist = quickExit.first(),
            continuousSearch = continuousSearch.first(),
            previewAutoScroll = previewAutoScroll.first(),
        )
    }.flowOn(coroutineDispatcher)

    private suspend fun updateConfig(config: LocalSettingsConfig) {
        withContext(coroutineDispatcher) {
            with(config) {
                theme?.let { updateTheme(it) }
                font?.let { updateFont(it) }
                language?.let { updateLanguage(it) }
                icon?.let { updateIcon(it) }
                vaultPasscode?.let { updateVaultPasscode(vaultPasscode) }
                sortingType?.let { updateSortingType(it) }
                sortingOrder?.let { updateSortingOrder(it) }
                isShowNotesCount?.let { updateIsShowNotesCount(it) }
                isDoNotDisturb?.let { updateIsDoNotDisturb(it) }
                isScreenOn?.let { updateIsScreenOn(it) }
                isFullScreen?.let { updateIsFullScreen(it) }
                mainInterfaceId?.let { updateMainInterfaceId(it) }
                quickNoteFolderId?.let { updateQuickNoteFolderId(it) }
                isRememberScrollingPosition?.let { updateIsRememberScrollingPosition(it) }
                screenBrightnessLevel?.let { updateScreenBrightnessLevel(it) }
                quickExist?.let { updateQuickExit(it) }
                continuousSearch?.let { updateContinuousSearch(it) }
                previewAutoScroll?.let { updatePreviewAutoScroll(it) }
            }
        }
    }

    override suspend fun clearSettings() {
        withContext(coroutineDispatcher) {
            localSettingsDataSource.clear()
        }
    }

    private fun String.toLongList() = split(", ").mapNotNull { it.toLongOrNull() }

}