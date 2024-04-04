package com.noto.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.Theme
import com.noto.app.domain.UserStatus
import com.noto.app.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val userStatus = settingsRepository.userStatus
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserStatus.New)

    val theme = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.Lazily, Theme.System)

    val vaultPasscode = settingsRepository.vaultPasscode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

}