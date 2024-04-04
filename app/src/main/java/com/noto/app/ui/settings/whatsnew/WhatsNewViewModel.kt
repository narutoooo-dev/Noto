package com.noto.app.ui.settings.whatsnew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.Release
import com.noto.app.domain.settings.SettingsRepository
import kotlinx.coroutines.launch

class WhatsNewViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    fun updateLastVersion() = viewModelScope.launch {
        settingsRepository.updateLastVersion(Release.Version.Current.format())
    }

}