package com.noto.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.components.TextFieldStatus
import com.noto.app.domain.model.*
import com.noto.app.domain.repository.*
import com.noto.app.toUiState
import com.noto.app.util.hash
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val labelRepository: LabelRepository,
    private val noteLabelRepository: NoteLabelRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val userState = userRepository.user
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    val userStatus = settingsRepository.userStatus
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserStatus.New)

    val theme = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.Lazily, Theme.System)

    val font = settingsRepository.font
        .stateIn(viewModelScope, SharingStarted.Lazily, Font.Nunito)

    val icon = settingsRepository.icon
        .stateIn(viewModelScope, SharingStarted.Eagerly, Icon.Futuristic)

    val isShowNotesCount = settingsRepository.isShowNotesCount
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isDoNotDisturb = settingsRepository.isDoNotDisturb
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isScreenOn = settingsRepository.isScreenOn
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isFullScreen = settingsRepository.isFullScreen
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val screenBrightnessLevel = settingsRepository.screenBrightnessLevel
        .stateIn(viewModelScope, SharingStarted.Eagerly, ScreenBrightnessLevel.System)

    val vaultPasscode = settingsRepository.vaultPasscode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val vaultTimeout = settingsRepository.vaultTimeout
        .stateIn(viewModelScope, SharingStarted.Lazily, VaultTimeout.Immediately)

    val isBioAuthEnabled = settingsRepository.isBioAuthEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val mainInterfaceId = settingsRepository.mainInterfaceId
        .stateIn(viewModelScope, SharingStarted.Eagerly, Folder.GeneralFolderId)

    val isRememberScrollingPosition = settingsRepository.isRememberScrollingPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val mutableIsImportFinished = MutableSharedFlow<Unit>(replay = Int.MAX_VALUE)
    val isImportFinished get() = mutableIsImportFinished.asSharedFlow()

    val quickNoteFolderId = settingsRepository.quickNoteFolderId
        .stateIn(viewModelScope, SharingStarted.Eagerly, Folder.GeneralFolderId)

    val quickExit = settingsRepository.quickExit
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val continuousSearch = settingsRepository.continuousSearch
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val previewAutoScroll = settingsRepository.previewAutoScroll
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val mutableName = MutableStateFlow("")
    val name get() = mutableName.asStateFlow()

    private val mutableNameStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val nameStatus get() = mutableNameStatus.asStateFlow()

    private val mutableNameState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val nameState get() = mutableNameState.asStateFlow()

    private val mutableEmail = MutableStateFlow("")
    val email get() = mutableEmail.asStateFlow()

    private val mutableEmailStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val emailStatus get() = mutableEmailStatus.asStateFlow()

    private val mutableEmailState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val emailState get() = mutableEmailState.asStateFlow()

    init {
        settingsRepository.name
            .filterNotNull()
            .onEach(::setName)
            .launchIn(viewModelScope)
    }

    fun toggleShowNotesCount() = viewModelScope.launch {
        settingsRepository.updateIsShowNotesCount(!isShowNotesCount.value)
    }

    fun toggleDoNotDisturb() = viewModelScope.launch {
        settingsRepository.updateIsDoNotDisturb(!isDoNotDisturb.value)
    }

    fun toggleScreenOn() = viewModelScope.launch {
        settingsRepository.updateIsScreenOn(!isScreenOn.value)
    }

    fun toggleFullScreen() = viewModelScope.launch {
        settingsRepository.updateIsFullScreen(!isFullScreen.value)
    }

    fun updateScreenBrightnessLevel(level: ScreenBrightnessLevel) = viewModelScope.launch {
        settingsRepository.updateScreenBrightnessLevel(level)
    }

    suspend fun exportJson(): String {
        return settingsRepository.exportNotoData()
    }

    suspend fun importJson(data: String) {
        settingsRepository.importNotoData(data)
    }

    fun setVaultPasscode(passcode: String) = viewModelScope.launch {
        settingsRepository.updateVaultPasscode(passcode.hash())
    }

    fun updateVaultTimeout(timeout: VaultTimeout) = viewModelScope.launch {
        settingsRepository.updateVaultTimeout(timeout)
    }

    fun toggleIsBioAuthEnabled() = viewModelScope.launch {
        settingsRepository.updateIsBioAuthEnabled(!isBioAuthEnabled.value)
    }

    fun updateLastVersion() = viewModelScope.launch {
        settingsRepository.updateLastVersion(Release.Version.Current.format())
    }

    fun setMainInterfaceId(folderId: Long) = viewModelScope.launch {
        settingsRepository.updateMainInterfaceId(folderId)
    }

    fun updateTheme(value: Theme) = viewModelScope.launch {
        settingsRepository.updateTheme(value)
    }

    fun updateFont(value: Font) = viewModelScope.launch {
        settingsRepository.updateFont(value)
    }

    fun updateLanguage(value: Language) = viewModelScope.launch {
        settingsRepository.updateLanguage(value)
    }

    fun updateIcon(value: Icon) = viewModelScope.launch {
        settingsRepository.updateIcon(value)
    }

    fun toggleRememberScrollingPosition() = viewModelScope.launch {
        settingsRepository.updateIsRememberScrollingPosition(!isRememberScrollingPosition.value)
    }

    fun toggleQuickExit() = viewModelScope.launch {
        settingsRepository.updateQuickExit(!quickExit.value)
    }

    fun toggleContinuousSearch() = viewModelScope.launch {
        settingsRepository.updateContinuousSearch(!continuousSearch.value)
    }

    fun togglePreviewAutoScroll() = viewModelScope.launch {
        settingsRepository.updatePreviewAutoScroll(!previewAutoScroll.value)
    }

    fun emitIsImportFinished() = viewModelScope.launch {
        mutableIsImportFinished.emit(Unit)
    }

    fun getFolderById(folderId: Long) = folderRepository.getFolderById(folderId)

    fun setQuickNoteFolderId(folderId: Long) = viewModelScope.launch {
        settingsRepository.updateQuickNoteFolderId(folderId)
    }

    fun disableVault() = viewModelScope.launch {
        folderRepository.getAllFolders().first()
            .map { it.copy(isVaulted = false) }
            .forEach { folderRepository.updateFolder(it) }
        settingsRepository.updateVaultPasscode(passcode = null)
        settingsRepository.updateVaultTimeout(timeout = VaultTimeout.Immediately)
        settingsRepository.updateScheduledVaultTimeout(timeout = null)
        settingsRepository.updateIsBioAuthEnabled(isEnabled = false)
        settingsRepository.updateIsVaultOpen(isOpen = false)
    }

    fun setName(name: String) {
        mutableName.value = name
    }

    fun setNameStatus(status: TextFieldStatus) {
        mutableNameStatus.value = status
    }

    fun updateName() = viewModelScope.launch {
        mutableNameState.value = UiState.Loading
        mutableNameState.value = userRepository.updateName(name.value.trim()).toUiState()
    }

    fun setEmail(email: String) {
        mutableEmail.value = email
    }

    fun setEmailStatus(status: TextFieldStatus) {
        mutableEmailStatus.value = status
    }

    fun updateEmail() = viewModelScope.launch {
        mutableEmailState.value = UiState.Loading
        mutableEmailState.value = userRepository.updateEmail(email.value.trim()).toUiState()
    }

    fun logOutUser() = viewModelScope.launch {
        userRepository.logOut()
        folderRepository.clearFolders()
        noteRepository.clearNotes()
        labelRepository.clearLabels()
        noteLabelRepository.clearNoteLabels()
        settingsRepository.clearSettings()
    }

    fun deleteUser() = viewModelScope.launch {
        logOutUser().join()
        userRepository.delete()
    }
}