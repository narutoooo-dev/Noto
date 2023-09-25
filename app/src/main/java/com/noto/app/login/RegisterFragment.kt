package com.noto.app.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.data.model.remote.ResponseException
import com.noto.app.domain.model.NotoColor
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor
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
                val passwordInfoText = stringResource(id = R.string.password_info)
                val invalidEmailText = stringResource(id = R.string.invalid_email)
                val invalidPasswordText = stringResource(id = R.string.password_requirements)
                val invalidNameText = stringResource(id = R.string.invalid_name)
                val invalidConfirmPasswordText = stringResource(id = R.string.invalid_confirm_password)
                val color = NotoColor.Teal.toColor()
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                val isPasswordVisible = rememberSaveable { mutableStateOf(false) }

                Screen(
                    title = stringResource(id = R.string.register),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    color = color,
                ) {
                    Text(
                        text = stringResource(id = R.string.intro_start_title),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(NotoTheme.dimensions.medium),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                    Column {
                        TextField(
                            value = name,
                            onValueChange = {
                                if (it.length <= Constants.NameMaxLength) {
                                    viewModel.setName(it)
                                }
                                viewModel.setNameStatus(TextFieldStatus.Empty)
                            },
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
                            modifier = Modifier.focusRequester(focusRequester)
                        )

                        DisposableEffect(focusRequester) {
                            focusRequester.requestFocus()
                            onDispose {
                                focusManager.clearFocus()
                            }
                        }

                        Spacer(Modifier.height(NotoTheme.dimensions.medium))

                        TextField(
                            value = email,
                            onValueChange = {
                                viewModel.setEmail(it)
                                viewModel.setEmailStatus(TextFieldStatus.Empty)
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
                            onValueChange = {
                                viewModel.setPassword(it)
                                viewModel.setPasswordStatus(TextFieldStatus.Empty)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            status = passwordStatus,
                            isPasswordVisible = isPasswordVisible,
                        )

                        Spacer(Modifier.height(NotoTheme.dimensions.medium))

                        PasswordTextField(
                            value = confirmPassword,
                            onValueChange = {
                                viewModel.setConfirmPassword(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = stringResource(id = R.string.confirm_password),
                            status = confirmPasswordStatus,
                            keyboardOptions = KeyboardOptions.Password.copy(imeAction = ImeAction.Done),
                            onDone = {
                                defaultKeyboardAction(ImeAction.Done)
                                focusManager.clearFocus()
                            },
                            isPasswordVisible = isPasswordVisible
                        )

                        Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                        Button(
                            text = stringResource(id = R.string.register),
                            onClick = {
                                val isNameInvalid = name.isBlank()
                                val isEmailInvalid =
                                    !email.matches(Constants.Regex.Email) || email.any { it.isWhitespace() }
                                val isPasswordInvalid = !password.matches(Constants.Regex.Password)
                                val isConfirmPasswordInvalid = confirmPassword != password
                                if (isNameInvalid) {
                                    viewModel.setNameStatus(TextFieldStatus.Error(invalidNameText))
                                }
                                if (isEmailInvalid) {
                                    viewModel.setEmailStatus(TextFieldStatus.Error(invalidEmailText))
                                }
                                if (isPasswordInvalid) {
                                    viewModel.setPasswordStatus(TextFieldStatus.Error(invalidPasswordText))
                                }
                                if (isConfirmPasswordInvalid) {
                                    viewModel.setPasswordStatus(TextFieldStatus.Error(invalidConfirmPasswordText))
                                }
                                if (!isNameInvalid && !isEmailInvalid && !isPasswordInvalid && !isConfirmPasswordInvalid) {
                                    viewModel.registerUser(name, email, password)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = color,
                            contentColor = Color.White,
                        )
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                    TextButton(
                        text = buildAnnotatedString {
                            append(stringResource(id = R.string.already_have_an_account))
                            withStyle(MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = color).toSpanStyle()) {
                                append(' ')
                                append(stringResource(id = R.string.login))
                            }
                        },
                        onClick = { navController?.navigateSafely(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                LaunchedEffect(Unit) {
                    viewModel.setConfirmPasswordStatus(TextFieldStatus.Info(passwordInfoText))
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
                                viewModel.setEmailStatus(
                                    TextFieldStatus.Error(
                                        stringResource(id = R.string.user_already_registered)
                                    )
                                )
                            }

                            ResponseException.Auth.InvalidEmail -> {
                                viewModel.setEmailStatus(TextFieldStatus.Error(invalidEmailText))
                            }

                            ResponseException.Auth.InvalidPassword -> {
                                viewModel.setPasswordStatus(TextFieldStatus.Error(invalidPasswordText))
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
}