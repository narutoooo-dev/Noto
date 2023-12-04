package com.noto.app.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.material.TextFieldStatus
import com.noto.app.domain.OtpType
import com.noto.app.domain.repository.UserRepository
import com.noto.app.toUiState
import com.noto.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VerifyOtpViewModel(
    private val userRepository: UserRepository,
    private val email: String,
    private val type: OtpType,
    private val sendOtp: Boolean,
) : ViewModel() {

    private val mutableState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val state get() = mutableState.asStateFlow()

    private val mutableOtp = MutableStateFlow("")
    val otp get() = mutableOtp.asStateFlow()

    private val mutableOtpStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val otpStatus get() = mutableOtpStatus.asStateFlow()

    init {
        if (sendOtp) {
            viewModelScope.launch {
                userRepository.sendOtp(email, type)
            }
        }
    }

    fun verifyOtp() = viewModelScope.launch {
        if (otp.value.isNotBlank()) {
            if (otp.value.length == Constants.OtpLength) {
                mutableState.value = UiState.Loading
                mutableState.value = userRepository.verifyOtp(email, type, otp.value)
                    .toUiState()
            } else {
                mutableOtpStatus.value = TextFieldStatus.Error(R.string.one_time_passcode_is_required)
            }
        } else {
            mutableOtpStatus.value = TextFieldStatus.Error(R.string.one_time_passcode_is_required)
        }
    }

    fun setOtp(otp: String) {
        if (otp.length <= Constants.OtpLength) {
            mutableOtp.value = otp
            mutableOtpStatus.value = TextFieldStatus.Empty
        }
    }

    fun setOtpStatus(status: TextFieldStatus) {
        mutableOtpStatus.value = status
    }

}