package com.noto.app.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.components.material.TextFieldStatus
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.repository.*
import com.noto.app.toUiState
import com.noto.app.util.Constants
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountSettingsViewModel(
    private val userRepository: UserRepository,
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val labelRepository: LabelRepository,
    private val settingsRepository: SettingsRepository,
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

    init {
        settingsRepository.name
            .filterNotNull()
            .onEach(::setName)
            .launchIn(viewModelScope)
    }

    fun setName(name: String) {
        if (name.length <= Constants.NameMaxLength) mutableName.value = name
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
        if (!email.value.matches(Constants.Regex.Email) || email.value.any { it.isWhitespace() }) {
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

}