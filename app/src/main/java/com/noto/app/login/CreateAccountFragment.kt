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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.components.screen.Screen
import com.noto.app.components.material.*
import com.noto.app.domain.model.NotoException
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateAccountFragment : Fragment() {

    private val viewModel by viewModel<CreateAccountViewModel>()

    @OptIn(ExperimentalTextApi::class)
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
                val passwordRequirementsAnnotatedString = remember {
                    context.getText(R.string.password_requirements).toSpannable().toAnnotatedString()
                }
                val uriHandler = LocalUriHandler.current

                Screen(
                    title = stringResource(id = R.string.create_account),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val accountAgreementAnnotatedString = remember {
                        context.getText(R.string.account_agreement).toSpannable().toAnnotatedString(primaryColor)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(id = R.string.create_account_description),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(NotoTheme.dimensions.extraLarge))

                        NotoTextField(
                            value = name,
                            onValueChange = viewModel::setName,
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

                        NotoTextField(
                            value = email,
                            onValueChange = viewModel::setEmail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState -> if (!focusState.isFocused) viewModel.validateEmail() },
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

                        NotoPasswordTextField(
                            value = password,
                            onValueChange = viewModel::setPassword,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState -> if (!focusState.isFocused) viewModel.validatePassword() },
                            status = passwordStatus,
                            isPasswordVisible = isPasswordVisible,
                        )

                        Spacer(Modifier.height(NotoTheme.dimensions.medium))

                        NotoPasswordTextField(
                            value = confirmPassword,
                            onValueChange = viewModel::setConfirmPassword,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState -> if (!focusState.isFocused) viewModel.validateConfirmPassword() },
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

                        ClickableText(
                            text = passwordRequirementsAnnotatedString,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline, textAlign = TextAlign.End),
                            onClick = {
                                navController?.navigateSafely(
                                    CreateAccountFragmentDirections.actionCreateAccountFragmentToPasswordRequirementsDialogFragment(password)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                        NotoButton(
                            text = stringResource(id = R.string.create_account),
                            onClick = viewModel::createAccount,
                            modifier = Modifier.fillMaxWidth(),
                            contentColor = Color.White,
                        )

                        Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                        ClickableText(
                            text = accountAgreementAnnotatedString,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                            ),
                            onClick = { offset ->
                                accountAgreementAnnotatedString.getUrlAnnotations(offset, offset).firstOrNull()?.let {
                                    uriHandler.openUri(it.item.url)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge * 2))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(id = R.string.already_have_an_account))
                        NotoOutlinedButton(
                            text = stringResource(id = R.string.log_in),
                            onClick = { navController?.navigateSafely(CreateAccountFragmentDirections.actionCreateAccountFragmentToLoginFragment()) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                state.fold(
                    onLoading = {
                        navController?.navigateSafely(
                            CreateAccountFragmentDirections.actionCreateAccountFragmentToProgressIndicatorDialogFragment(
                                stringResource(id = R.string.creating_account)
                            )
                        )
                    },
                    onSuccess = {
                        if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                            navController?.navigateUp()

                        navController?.navigateSafely(CreateAccountFragmentDirections.actionCreateAccountFragmentToVerifyEmailDialogFragment(email))
                    },
                    onFailure = { exception ->
                        if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                            navController?.navigateUp()
                        when (exception) {
                            NotoException.Auth.UserAlreadyExists -> {
                                viewModel.setEmailStatus(TextFieldStatus.Error(R.string.user_already_exists))
                            }

                            NotoException.Auth.InvalidEmail -> {
                                viewModel.setEmailStatus(TextFieldStatus.Error(R.string.email_is_invalid))
                            }

                            NotoException.Auth.InvalidPassword -> {
                                viewModel.setPasswordStatus(TextFieldStatus.Error(R.string.password_is_invalid_requirements))
                            }

                            is NotoException.TryAgainLater -> {
                                val message = stringResource(id = R.string.try_again_in)
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