package com.noto.app.settings.readingmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.model.ScreenBrightnessLevel
import com.noto.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadingModeSettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val isDoNotDisturb = settingsRepository.isDoNotDisturb
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isScreenOn = settingsRepository.isScreenOn
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isFullScreen = settingsRepository.isFullScreen
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val screenBrightnessLevel = settingsRepository.screenBrightnessLevel
        .stateIn(viewModelScope, SharingStarted.Eagerly, ScreenBrightnessLevel.System)

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

}