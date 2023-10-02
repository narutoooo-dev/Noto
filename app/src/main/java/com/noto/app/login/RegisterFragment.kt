package com.noto.app.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.data.model.remote.ResponseException
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.util.Constants
import com.noto.app.util.navController
import com.noto.app.util.navigateSafely
import com.noto.app.util.setupMixedTransitions
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {

    private val viewModel by viewModel<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        setupMixedTransitions()
        activity?.onBackPressedDispatcher?.addCallback { navController?.navigateUp() }
        ComposeView(context).apply {
            isTransitionGroup = true
            setContent {
                val state by viewModel.state.collectAsState()
                val name by viewModel.name.collectAsState()
                val email by viewModel.email.collectAsState()
                val password by viewModel.password.collectAsState()
                val confirmPassword by viewModel.confirmPassword.collectAsState()
                val nameStatus by viewModel.nameStatus.collectAsState()
                val emailStatus by viewModel.emailStatus.collectAsState()
                val passwordStatus by viewModel.passwordStatus.collectAsState()
                val confirmPasswordStatus by viewModel.confirmPasswordStatus.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val focusManager = LocalFocusManager.current
                val isPasswordVisible = rememberSaveable { mutableStateOf(false) }
                val passwordRequirementsInteractionSource = remember { MutableInteractionSource() }

                Screen(
                    title = stringResource(id = R.string.register),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TextField(
                            value = name,
                            onValueChange = { name ->
                                if (name.length <= Constants.NameMaxLength) {
                                    viewModel.setName(name)
                                }
                                if (name.isNotBlank()) {
                                    viewModel.setNameStatus(TextFieldStatus.Empty)
                                } else {
                                    viewModel.setNameStatus(TextFieldStatus.Error(R.string.name_is_required))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = stringResource(id = R.string.name),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_round_person_24),
                                    contentDescription = stringResource(id = R.string.name)
                                )
                            },
                            status = nameStatus,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next,
                            ),
                        )

                        Spacer(Modifier.height(NotoTheme.dimensions.medium))

                        TextField(
                            value = email,
                            onValueChange = { email ->
                                viewModel.setEmail(email)
                                if (email.isNotBlank()) {
                                    val isEmailValid = Constants.Regex.matchesEmail(email)
                                    if (isEmailValid) {
                                        viewModel.setEmailStatus(TextFieldStatus.Empty)
                                    } else if (emailStatus.isError) {
                                        viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_invalid))
                                    }
                                } else {
                                    viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_required))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused && email.isNotBlank()) {
                                        val isEmailValid = Constants.Regex.matchesEmail(email)
                                        if (!isEmailValid) viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_invalid))
                                    }
                                },
                            placeholder = stringResource(id = R.string.email),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_round_email_24),
                                    contentDescription = stringResource(id = R.string.email),
                                )
                            },
                            status = emailStatus,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                        )

                        Spacer(Modifier.height(NotoTheme.dimensions.medium))

                        PasswordTextField(
                            value = password,
                            onValueChange = { password ->
                                viewModel.setPassword(password)
                                if (password.isNotBlank()) {
                                    val isPasswordValid = Constants.Regex.matchesPassword(password)
                                    if (isPasswordValid) {
                                        viewModel.setPasswordStatus(TextFieldStatus.Empty)
                                    } else if (passwordStatus.isError) {
                                        viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_invalid_requirements))
                                    }
                                    if (confirmPassword.isNotBlank()) {
                                        val isConfirmPasswordValid = confirmPassword == password
                                        if (isConfirmPasswordValid) {
                                            viewModel.setConfirmPasswordStatus(TextFieldStatus.Empty)
                                        } else {
                                            viewModel.setConfirmPasswordStatus(TextFieldStatus.Error(R.string.confirm_password_is_invalid))
                                        }
                                    }
                                } else {
                                    viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_required))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused && password.isNotBlank()) {
                                        val isPasswordValid = Constants.Regex.matchesPassword(password)
                                        if (!isPasswordValid) viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_invalid_requirements))
                                    }
                                },
                            status = passwordStatus,
                            isPasswordVisible = isPasswordVisible,
                        )

                        Spacer(Modifier.height(NotoTheme.dimensions.medium))

                        PasswordTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword ->
                                viewModel.setConfirmPassword(confirmPassword)
                                if (confirmPassword.isNotBlank()) {
                                    val isConfirmPasswordValid = confirmPassword == password
                                    if (isConfirmPasswordValid) {
                                        viewModel.setConfirmPasswordStatus(TextFieldStatus.Empty)
                                    } else if (confirmPasswordStatus.isError) {
                                        viewModel.setConfirmPasswordStatus(TextFieldStatus.Error(R.string.confirm_password_is_invalid))
                                    }
                                } else {
                                    viewModel.setConfirmPasswordStatus(TextFieldStatus.Error(R.string.confirm_password_is_required))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused && confirmPassword.isNotBlank()) {
                                        val isConfirmPasswordValid = confirmPassword == password
                                        if (!isConfirmPasswordValid)
                                            viewModel.setConfirmPasswordStatus(TextFieldStatus.Error(R.string.confirm_password_is_invalid))
                                    }
                                },
                            placeholder = stringResource(id = R.string.confirm_password),
                            status = confirmPasswordStatus,
                            keyboardOptions = KeyboardOptions.Password.copy(imeAction = ImeAction.Done),
                            onDone = {
                                defaultKeyboardAction(ImeAction.Done)
                                focusManager.clearFocus()
                            },
                            isPasswordVisible = isPasswordVisible,
                        )


                        Spacer(Modifier.height(NotoTheme.dimensions.medium))

                        Text(
                            text = stringResource(id = R.string.password_requirements),
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable(interactionSource = passwordRequirementsInteractionSource, indication = null) {
                                    navController?.navigateSafely(
                                        RegisterFragmentDirections.actionRegisterFragmentToPasswordRequirementsDialogFragment(
                                            password
                                        )
                                    )
                                },
                            style = MaterialTheme.typography.labelLarge,
                            textDecoration = TextDecoration.Underline,
                        )

                        Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                        Button(
                            text = stringResource(id = R.string.register),
                            onClick = {
                                val isInputValid = checkIsInputValid(name, email, password, confirmPassword)
                                if (isInputValid) viewModel.registerUser(name, email, password)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentColor = Color.White,
                        )
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(id = R.string.already_have_an_account))
                        OutlinedButton(
                            text = stringResource(id = R.string.login),
                            onClick = { navController?.navigateSafely(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                state.fold(
                    onLoading = {
                        navController?.navigateSafely(
                            RegisterFragmentDirections.actionRegisterFragmentToProgressIndicatorDialogFragment(
                                stringResource(id = R.string.creating_account)
                            )
                        )
                    },
                    onSuccess = {
                        if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                            navController?.navigateUp()

                        navController?.navigateSafely(RegisterFragmentDirections.actionRegisterFragmentToVerifyEmailDialogFragment())
                    },
                    onFailure = { exception ->
                        if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                            navController?.navigateUp()
                        when (exception) {
                            ResponseException.Auth.UserAlreadyRegistered -> {
                                viewModel.setEmailStatus(TextFieldStatus.Error(R.string.user_already_registered))
                            }

                            ResponseException.Auth.InvalidEmail -> {
                                viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_invalid))
                            }

                            ResponseException.Auth.InvalidPassword -> {
                                viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_invalid_requirements))
                            }

                            is ResponseException.Auth.TooManyRequests -> {
                                val message = stringResource(id = R.string.try_again_in, exception.seconds)
                                LaunchedEffect(key1 = exception) {
                                    snackbarHostState.showSnackbar(message = message)
                                }
                            }

                            else -> {
                                val message = stringResource(id = R.string.something_went_wrong)
                                val actionLabel = stringResource(id = R.string.show_info)
                                LaunchedEffect(key1 = exception) {
                                    val result = snackbarHostState.showSnackbar(
                                        message = message,
                                        actionLabel = actionLabel,
                                    )
                                    when (result) {
                                        SnackbarResult.Dismissed -> {}
                                        SnackbarResult.ActionPerformed -> {
                                            // TODO Navigate to info dialog and show the exception.
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    private fun checkIsInputValid(name: String, email: String, password: String, confirmPassword: String): Boolean {
        val isNameValid = name.isNotBlank()
        val isEmailValid = Constants.Regex.matchesEmail(email)
        val isPasswordValid = Constants.Regex.matchesPassword(password)
        val isConfirmPasswordValid = confirmPassword == password

        if (name.isBlank()) viewModel.setNameStatus(TextFieldStatus.Error(R.string.name_is_required))

        if (email.isBlank()) {
            viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_required))
        } else {
            if (!isEmailValid) viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_invalid))
        }

        if (password.isBlank()) {
            viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_required))
        } else {
            if (!isPasswordValid) viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_invalid_requirements))
        }

        if (confirmPassword.isBlank()) {
            viewModel.setConfirmPasswordStatus(TextFieldStatus.Error(R.string.confirm_password_is_required))
        } else {
            if (!isConfirmPasswordValid) viewModel.setConfirmPasswordStatus(TextFieldStatus.Error(R.string.confirm_password_is_invalid))
        }

        return isNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
    }
}