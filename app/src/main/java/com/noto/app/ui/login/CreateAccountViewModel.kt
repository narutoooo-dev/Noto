package com.noto.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.R
import com.noto.app.domain.PropertyConstants
import com.noto.app.domain.RegexConstants
import com.noto.app.domain.UserStatus
import com.noto.app.domain.settings.SettingsRepository
import com.noto.app.domain.user.UserRepository
import com.noto.app.ui.UiState
import com.noto.app.ui.component.material.TextFieldStatus
import com.noto.app.ui.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateAccountViewModel(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val state get() = mutableState.asStateFlow()

    private val mutableName = MutableStateFlow("")
    val name get() = mutableName.asStateFlow()

    private val mutableEmail = MutableStateFlow("")
    val email get() = mutableEmail.asStateFlow()

    private val mutablePassword = MutableStateFlow("")
    val password get() = mutablePassword.asStateFlow()

    private val mutableConfirmPassword = MutableStateFlow("")
    val confirmPassword get() = mutableConfirmPassword.asStateFlow()

    private val mutableNameStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val nameStatus get() = mutableNameStatus.asStateFlow()

    private val mutableEmailStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val emailStatus get() = mutableEmailStatus.asStateFlow()

    private val mutablePasswordStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val passwordStatus get() = mutablePasswordStatus.asStateFlow()

    private val mutableConfirmPasswordStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val confirmPasswordStatus get() = mutableConfirmPasswordStatus.asStateFlow()

    fun createAccount() = viewModelScope.launch {
        val isInputValid = checkIsInputValid(name.value, email.value, password.value, confirmPassword.value)
        if (isInputValid) {
            mutableState.value = UiState.Loading
            mutableState.value = userRepository.createAccount(name.value, email.value, password.value).toUiState()
        }
    }

    fun setName(name: String) {
        if (name.length <= PropertyConstants.NameMaxLength) {
            mutableName.value = name
        }
        mutableNameStatus.value = if (name.isNotBlank()) {
            TextFieldStatus.Empty
        } else {
            TextFieldStatus.Error(R.string.name_is_required)
        }
    }

    fun setEmail(email: String) {
        mutableEmail.value = email
        if (email.isNotBlank()) {
            val isEmailValid = RegexConstants.matchesEmail(email)
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
            val isPasswordValid = RegexConstants.matchesPassword(password)
            if (isPasswordValid) {
                mutablePasswordStatus.value = TextFieldStatus.Empty
            } else if (passwordStatus.value.isError) {
                mutablePasswordStatus.value = TextFieldStatus.Error(R.string.password_is_invalid_requirements)
            }
            if (confirmPassword.value.isNotBlank()) {
                val isConfirmPasswordValid = confirmPassword.value == password
                if (isConfirmPasswordValid) {
                    mutableConfirmPasswordStatus.value = TextFieldStatus.Empty
                } else {
                    mutableConfirmPasswordStatus.value = TextFieldStatus.Error(R.string.confirm_password_is_invalid)
                }
            }
        } else {
            mutablePasswordStatus.value = TextFieldStatus.Error(R.string.password_is_required)
        }
    }

    fun setConfirmPassword(confirmPassword: String) {
        mutableConfirmPassword.value = confirmPassword
        if (confirmPassword.isNotBlank()) {
            val isConfirmPasswordValid = confirmPassword == password.value
            if (isConfirmPasswordValid) {
                mutableConfirmPasswordStatus.value = TextFieldStatus.Empty
            } else if (confirmPasswordStatus.value.isError) {
                mutableConfirmPasswordStatus.value = TextFieldStatus.Error(R.string.confirm_password_is_invalid)
            }
        } else {
            mutableConfirmPasswordStatus.value = TextFieldStatus.Error(R.string.confirm_password_is_required)
        }
    }

    fun setEmailStatus(status: TextFieldStatus) {
        mutableEmailStatus.value = status
    }

    fun setPasswordStatus(status: TextFieldStatus) {
        mutablePasswordStatus.value = status
    }

    fun markEmailAsVerified() = viewModelScope.launch {
        settingsRepository.updateUserStatus(UserStatus.LoggedIn)
    }

    fun validateEmail() {
        if (email.value.isNotBlank()) {
            val isEmailValid = RegexConstants.matchesEmail(email.value)
            if (!isEmailValid) mutableEmailStatus.value = TextFieldStatus.Error(R.string.email_is_invalid)
        }
    }

    fun validatePassword() {
        if (password.value.isNotBlank()) {
            val isPasswordValid = RegexConstants.matchesPassword(password.value)
            if (!isPasswordValid) mutablePasswordStatus.value = TextFieldStatus.Error(R.string.password_is_invalid_requirements)
        }
    }

    fun validateConfirmPassword() {
        if (confirmPassword.value.isNotBlank()) {
            val isConfirmPasswordValid = confirmPassword.value == password.value
            if (!isConfirmPasswordValid) mutableConfirmPasswordStatus.value = TextFieldStatus.Error(R.string.confirm_password_is_invalid)
        }
    }

    private fun checkIsInputValid(name: String, email: String, password: String, confirmPassword: String): Boolean {
        val isNameValid = name.isNotBlank()
        val isEmailValid = RegexConstants.matchesEmail(email)
        val isPasswordValid = RegexConstants.matchesPassword(password)
        val isConfirmPasswordValid = confirmPassword == password

        if (name.isBlank()) mutableNameStatus.value = TextFieldStatus.Error(R.string.name_is_required)

        if (email.isBlank()) {
            mutableEmailStatus.value = TextFieldStatus.Error(R.string.email_is_required)
        } else {
            if (!isEmailValid) mutableEmailStatus.value = TextFieldStatus.Error(R.string.email_is_invalid)
        }

        if (password.isBlank()) {
            mutablePasswordStatus.value = TextFieldStatus.Error(R.string.password_is_required)
        } else {
            if (!isPasswordValid) mutablePasswordStatus.value = TextFieldStatus.Error(R.string.password_is_invalid_requirements)
        }

        if (confirmPassword.isBlank()) {
            mutableConfirmPasswordStatus.value = TextFieldStatus.Error(R.string.confirm_password_is_required)
        } else {
            if (!isConfirmPasswordValid) mutableConfirmPasswordStatus.value = TextFieldStatus.Error(R.string.confirm_password_is_invalid)
        }

        return isNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
    }

}