package com.noto.app.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.TextFieldStatus
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.model.UserStatus
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.repository.UserRepository
import com.noto.app.toUiState
import com.noto.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VerifyEmailViewModel(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val email: String,
) : ViewModel() {

    private val mutableState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val state get() = mutableState.asStateFlow()

    private val mutableOtp = MutableStateFlow("")
    val otp get() = mutableOtp.asStateFlow()

    private val mutableOtpStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val otpStatus get() = mutableOtpStatus.asStateFlow()

    fun verifyEmail() = viewModelScope.launch {
        if (otp.value.isNotBlank()) {
            mutableState.value = UiState.Loading
            mutableState.value = userRepository.verifyEmail(email, otp.value)
                .onSuccess { settingsRepository.updateUserStatus(UserStatus.LoggedIn) }
                .toUiState()
        } else {
            mutableOtpStatus.value = TextFieldStatus.Error(R.string.one_time_passcode_is_required)
        }
    }

    fun setOtp(otp: String) {
        if (otp.length <= Constants.OtpMaxLength) {
            mutableOtp.value = otp
            mutableOtpStatus.value = TextFieldStatus.Empty
        }
    }

    fun setOtpStatus(status: TextFieldStatus) {
        mutableOtpStatus.value = status
    }

}