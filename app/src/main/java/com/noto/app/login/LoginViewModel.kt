package com.noto.app.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.TextFieldStatus
import com.noto.app.domain.model.UserStatus
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.repository.UserRepository
import com.noto.app.toUiState
import com.noto.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val state get() = mutableState.asStateFlow()

    private val mutableEmail = MutableStateFlow("")
    val email get() = mutableEmail.asStateFlow()

    private val mutablePassword = MutableStateFlow("")
    val password get() = mutablePassword.asStateFlow()

    private val mutableEmailStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val emailStatus get() = mutableEmailStatus.asStateFlow()

    private val mutablePasswordStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val passwordStatus get() = mutablePasswordStatus.asStateFlow()

    fun logIn() = viewModelScope.launch {
        val isInputValid = checkIsInputValid()
        if (isInputValid) {
            mutableState.value = UiState.Loading
            userRepository.logIn(email.value, password.value)
                .onSuccess { settingsRepository.updateUserStatus(UserStatus.LoggedIn) }
                .toUiState()
        }
    }

    fun setEmail(email: String) {
        mutableEmail.value = email
        if (email.isNotBlank()) {
            val isEmailValid = Constants.Regex.matchesEmail(email)
            if (isEmailValid) {
                mutableEmailStatus.value = TextFieldStatus.Empty
            } else if (emailStatus.value.isError) {
                mutableEmailStatus.value = TextFieldStatus.Error(R.string.email_is_invalid)
            }
        } else {
            mutableEmailStatus.value = TextFieldStatus.Error(R.string.email_is_required)
        }
    }

    fun setPassword(password: String) {
        mutablePassword.value = password
        if (password.isNotBlank()) {
            mutablePasswordStatus.value = TextFieldStatus.Empty
        } else {
            mutablePasswordStatus.value = TextFieldStatus.Error(R.string.password_is_required)
        }
    }

    fun validateEmail() {
        if (email.value.isNotBlank()) {
            val isEmailValid = Constants.Regex.matchesEmail(email.value)
            if (!isEmailValid) mutableEmailStatus.value = TextFieldStatus.Error(R.string.email_is_invalid)
        }
    }

    private fun checkIsInputValid(): Boolean {
        val isEmailValid = Constants.Regex.matchesEmail(email.value)
        val isPasswordValid = password.value.isNotBlank()

        if (email.value.isBlank()) {
            mutableEmailStatus.value = TextFieldStatus.Error(R.string.email_is_required)
        } else {
            if (!isEmailValid) mutableEmailStatus.value = TextFieldStatus.Error(R.string.email_is_invalid)
        }

        if (!isPasswordValid) mutablePasswordStatus.value = TextFieldStatus.Error(R.string.password_is_required)

        return isEmailValid && isPasswordValid
    }

}