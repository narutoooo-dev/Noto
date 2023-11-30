package com.noto.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.model.Theme
import com.noto.app.domain.model.UserStatus
import com.noto.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val userStatus = settingsRepository.userStatus
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserStatus.New)

    val theme = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.Lazily, Theme.System)

    val isShowNotesCount = settingsRepository.isShowNotesCount
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val vaultPasscode = settingsRepository.vaultPasscode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isRememberScrollingPosition = settingsRepository.isRememberScrollingPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val quickExit = settingsRepository.quickExit
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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