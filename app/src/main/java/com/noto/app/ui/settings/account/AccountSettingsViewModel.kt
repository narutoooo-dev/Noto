package com.noto.app.ui.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.data.sync.ManualSyncServiceManager
import com.noto.app.domain.NotoException
import com.noto.app.domain.PropertyConstants
import com.noto.app.domain.RegexConstants
import com.noto.app.domain.folder.FolderRepository
import com.noto.app.domain.label.LabelRepository
import com.noto.app.domain.note.NoteRepository
import com.noto.app.domain.settings.SettingsRepository
import com.noto.app.domain.user.UserRepository
import com.noto.app.ui.UiState
import com.noto.app.ui.component.material.TextFieldStatus
import com.noto.app.ui.toUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AccountSettingsViewModel(
    private val userRepository: UserRepository,
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val labelRepository: LabelRepository,
    private val settingsRepository: SettingsRepository,
    private val manualSyncServiceManager: ManualSyncServiceManager,
) : ViewModel() {

    val userState = userRepository.user
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

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

    private val mutableManualSyncState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val manualSyncState get() = mutableManualSyncState.asStateFlow()

    val lastSyncTimestamp = settingsRepository.lastSyncTimestamp
        .stateIn(viewModelScope, SharingStarted.Eagerly, Clock.System.now())

    init {
        settingsRepository.name
            .filterNotNull()
            .onEach(::setName)
            .launchIn(viewModelScope)
    }

    fun setName(name: String) {
        if (name.length <= PropertyConstants.NameMaxLength) mutableName.value = name
        mutableNameStatus.value = TextFieldStatus.Empty
    }

    fun setNameStatus(status: TextFieldStatus) {
        mutableNameStatus.value = status
    }

    fun updateName() = viewModelScope.launch {
        if (name.value.isNotBlank()) {
            mutableNameState.value = UiState.Loading
            mutableNameState.value = userRepository.updateName(name.value.trim()).toUiState()
        } else {
            mutableNameState.value = UiState.Failure(NotoException.Model.NameIsRequired)
        }
    }

    fun setEmail(email: String) {
        mutableEmail.value = email
    }

    fun setEmailStatus(status: TextFieldStatus) {
        mutableEmailStatus.value = status
    }

    fun updateEmail() = viewModelScope.launch {
        if (!email.value.matches(RegexConstants.Email) || email.value.any { it.isWhitespace() }) {
            mutableEmailState.value = UiState.Failure(NotoException.Auth.InvalidEmail)
        } else {
            mutableEmailState.value = UiState.Loading
            mutableEmailState.value = userRepository.updateEmail(email.value.trim()).toUiState()
        }
    }

    fun logOutUser() = viewModelScope.launch {
        userRepository.logOut()
        folderRepository.clearFolders()
        noteRepository.clearNotes()
        labelRepository.clearLabels()
        settingsRepository.clearSettings()
    }

    fun deleteUser() = viewModelScope.launch {
        logOutUser().join()
        userRepository.delete()
    }

    fun runManualSync() = viewModelScope.launch {
        mutableManualSyncState.value = UiState.Loading
        mutableManualSyncState.value = manualSyncServiceManager.runManualSyncServices().toUiState()
    }

}