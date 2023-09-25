package com.noto.app.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

class LoginFragment : Fragment() {

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
                val email by viewModel.email.collectAsState()
                val password by viewModel.password.collectAsState()
                val emailStatus by viewModel.emailStatus.collectAsState()
                val passwordStatus by viewModel.passwordStatus.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val invalidEmailText = stringResource(id = R.string.invalid_email)
                val invalidPasswordText = stringResource(id = R.string.invalid_password)
                val color = NotoColor.Teal.toColor()
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                Screen(
                    title = stringResource(id = R.string.login),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    color = color,
                ) {
                    Text(
                        text = stringResource(id = R.string.welcome_back),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(NotoTheme.dimensions.medium),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(Modifier.height(NotoTheme.dimensions.extraLarge))

                    Column {
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
                            modifier = Modifier.focusRequester(focusRequester)
                        )

                        DisposableEffect(focusRequester) {
                            focusRequester.requestFocus()
                            onDispose {
                                focusManager.clearFocus(force = true)
                            }
                        }

                        Spacer(Modifier.height(NotoTheme.dimensions.medium))

                        PasswordTextField(
                            value = password,
                            onValueChange = {
                                viewModel.setPassword(it)
                                viewModel.setPasswordStatus(TextFieldStatus.Empty)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            status = passwordStatus,
                            keyboardOptions = KeyboardOptions.Password.copy(imeAction = ImeAction.Done),
                            onDone = {
                                defaultKeyboardAction(ImeAction.Done)
                                focusManager.clearFocus()
                            }
                        )

                        Spacer(Modifier.height(NotoTheme.dimensions.extraLarge))

                        Button(
                            text = stringResource(id = R.string.login),
                            onClick = {
                                val isEmailInvalid = !email.matches(Constants.Regex.Email) || email.any { it.isWhitespace() }
                                val isPasswordInvalid = password.isBlank()
                                if (isEmailInvalid) {
                                    viewModel.setEmailStatus(TextFieldStatus.Error(invalidEmailText))
                                }
                                if (isPasswordInvalid) {
                                    viewModel.setPasswordStatus(TextFieldStatus.Error(invalidPasswordText))
                                }
                                if (!isEmailInvalid && !isPasswordInvalid) {
                                    viewModel.loginUser(email, password)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = color,
                            contentColor = Color.White,
                        )

                        TextButton(
                            text = stringResource(id = R.string.forgot_password),
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(NotoTheme.dimensions.medium, NotoTheme.dimensions.extraSmall),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(Modifier.height(NotoTheme.dimensions.extraLarge))

                    TextButton(
                        text = buildAnnotatedString {
                            append(stringResource(id = R.string.dont_have_account))
                            withStyle(MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = color).toSpanStyle()) {
                                append(' ')
                                append(stringResource(id = R.string.register))
                            }
                        },
                        onClick = { navController?.navigateSafely(LoginFragmentDirections.actionLoginFragmentToRegisterFragment()) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                state.fold(
                    onLoading = {
                        navController?.navigateSafely(
                            LoginFragmentDirections.actionLoginFragmentToProgressIndicatorDialogFragment(
                                stringResource(id = R.string.logging_in)
                            )
                        )
                    },
                    onSuccess = {
                        if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                            navController?.navigateUp()
                    },
                    onFailure = { exception ->
                        if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                            navController?.navigateUp()
                        when (exception) {
                            ResponseException.Auth.InvalidLoginCredentials -> {
                                val message = stringResource(id = R.string.invalid_credentials)
                                LaunchedEffect(key1 = exception) {
                                    snackbarHostState.showSnackbar(message = message)
                                }
                            }

                            ResponseException.Auth.EmailNotVerified -> {
                                val message = stringResource(id = R.string.email_not_verified)
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
