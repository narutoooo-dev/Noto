package com.noto.app.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.data.model.remote.ResponseException
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {

    private val viewModel by viewModel<LoginViewModel>()

    private val isIntro by lazy { navController?.previousBackStackEntry?.destination?.id == R.id.startFragment }

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
                val email by viewModel.email.collectAsState()
                val password by viewModel.password.collectAsState()
                val emailStatus by viewModel.emailStatus.collectAsState()
                val passwordStatus by viewModel.passwordStatus.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val focusManager = LocalFocusManager.current
                val forgotPasswordAnnotatedString = remember { context.getText(R.string.forgot_password).toSpannable().toAnnotatedString() }

                Screen(
                    title = stringResource(id = R.string.login),
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

                        Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                        PasswordTextField(
                            value = password,
                            onValueChange = { password ->
                                viewModel.setPassword(password)
                                if (password.isNotBlank()) {
                                    viewModel.setPasswordStatus(TextFieldStatus.Empty)
                                } else {
                                    viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_required))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            status = passwordStatus,
                            keyboardOptions = KeyboardOptions.Password.copy(imeAction = ImeAction.Done),
                            onDone = {
                                defaultKeyboardAction(ImeAction.Done)
                                focusManager.clearFocus()
                            }
                        )

                        Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                        ClickableText(
                            text = forgotPasswordAnnotatedString,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline, textAlign = TextAlign.End),
                            onClick = {},
                        )

                        Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                        Button(
                            text = stringResource(id = R.string.login),
                            onClick = {
                                val isInputValid = checkIsInputValid(email, password)
                                if (isInputValid) viewModel.login(email, password)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (!isIntro) {
                        Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge * 2))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(id = R.string.dont_have_account))
                            OutlinedButton(
                                text = stringResource(id = R.string.create_account),
                                onClick = { navController?.navigateSafely(LoginFragmentDirections.actionLoginFragmentToCreateAccountFragment()) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
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

    private fun checkIsInputValid(email: String, password: String): Boolean {
        val isEmailValid = Constants.Regex.matchesEmail(email)
        val isPasswordValid = password.isNotBlank()

        if (email.isBlank()) {
            viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_required))
        } else {
            if (!isEmailValid) viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_invalid))
        }

        if (!isPasswordValid) viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_required))

        return isEmailValid && isPasswordValid
    }
}
