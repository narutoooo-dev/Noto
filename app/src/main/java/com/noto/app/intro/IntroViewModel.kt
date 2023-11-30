package com.noto.app.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.model.Theme
import com.noto.app.domain.model.UserStatus
import com.noto.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IntroViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val userStatus = settingsRepository.userStatus
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val vaultPasscode = settingsRepository.vaultPasscode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val mutableIsNotificationPermissionGranted = MutableStateFlow<Boolean?>(null)
    val isNotificationPermissionGranted get() = mutableIsNotificationPermissionGranted.asStateFlow()

    val theme = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.Lazily, Theme.System)

    val isShowNotesCount = settingsRepository.isShowNotesCount
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isRememberScrollingPosition = settingsRepository.isRememberScrollingPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val quickExit = settingsRepository.quickExit
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setNotificationPermissionResult(isGranted: Boolean?) {
        mutableIsNotificationPermissionGranted.value = isGranted
    }

    fun finishIntro() = viewModelScope.launch {
        val currentUserStatus = settingsRepository.userStatus.first()
        if (currentUserStatus == UserStatus.New) settingsRepository.updateUserStatus(UserStatus.NotLoggedIn)
    }

    fun toggleShowNotesCount() = viewModelScope.launch {
        settingsRepository.updateIsShowNotesCount(!isShowNotesCount.value)
    }

    fun toggleRememberScrollingPosition() = viewModelScope.launch {
        settingsRepository.updateIsRememberScrollingPosition(!isRememberScrollingPosition.value)
    }

    fun toggleQuickExit() = viewModelScope.launch {
        settingsRepository.updateQuickExit(!quickExit.value)
    }

}