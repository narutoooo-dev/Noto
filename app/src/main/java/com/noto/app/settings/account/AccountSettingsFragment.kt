package com.noto.app.settings.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.components.material.ScreenCircularProgressIndicator
import com.noto.app.components.screen.Screen
import com.noto.app.domain.OtpType
import com.noto.app.fold
import com.noto.app.settings.SettingsItem
import com.noto.app.settings.SettingsItemType
import com.noto.app.settings.SettingsSection
import com.noto.app.theme.warning
import com.noto.app.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountSettingsFragment : Fragment() {

    private val viewModel by viewModel<AccountSettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        activity?.onBackPressedDispatcher?.addCallback {
            navController?.navigateUp()
        }

        navController?.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(Constants.LogOut)
            ?.observe(viewLifecycleOwner) { logOut() }

        navController?.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(Constants.DeleteAccount)
            ?.observe(viewLifecycleOwner) { deleteUser() }

        setupMixedTransitions()
        ComposeView(context).apply {
            isTransitionGroup = true
            setContent {
                val userState by viewModel.userState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val isOtpVerified by remember {
                    navController?.currentBackStackEntry?.savedStateHandle
                        ?.getStateFlow<Boolean?>(Constants.VerifyOtp, null) ?: MutableStateFlow(null)
                }.collectAsState()

                val isRequestConfirmed by remember {
                    navController?.currentBackStackEntry?.savedStateHandle
                        ?.getStateFlow(Constants.IsConfirmed, false) ?: MutableStateFlow(false)
                }.collectAsState()

                Screen(
                    title = stringResource(id = R.string.account),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) {
                    userState.fold(
                        onEmpty = {},
                        onLoading = { ScreenCircularProgressIndicator() },
                        onSuccess = { user ->
                            SettingsSection {
                                SettingsItem(
                                    title = stringResource(id = R.string.name),
                                    type = SettingsItemType.Text(user.name),
                                    onClick = { navController?.navigateSafely(AccountSettingsFragmentDirections.actionAccountSettingsFragmentToEditNameDialogFragment()) },
                                )
                                SettingsItem(
                                    title = stringResource(id = R.string.email),
                                    type = SettingsItemType.Text(user.email),
                                    onClick = { navController?.navigateSafely(AccountSettingsFragmentDirections.actionAccountSettingsFragmentToChangeEmailDialogFragment()) },
                                )
                            }

                            SettingsSection {
                                val confirmationText = stringResource(id = R.string.log_out_confirmation)
                                val descriptionText = stringResource(id = R.string.log_out_description)
                                val logOutText = stringResource(id = R.string.log_out)
                                SettingsItem(
                                    title = logOutText,
                                    type = SettingsItemType.None,
                                    onClick = {
                                        navController?.navigateSafely(
                                            AccountSettingsFragmentDirections.actionAccountSettingsFragmentToConfirmationDialogFragment(
                                                confirmation = confirmationText,
                                                description = descriptionText,
                                                btnText = logOutText,
                                                key = Constants.LogOut,
                                            )
                                        )
                                    },
                                    titleColor = MaterialTheme.colorScheme.warning,
                                )
                            }

                            Spacer(Modifier.weight(1F))

                            SettingsSection {
                                val title = stringResource(id = R.string.confirm_your_request)
                                val confirmation = stringResource(id = R.string.delete_account_confirmation)
                                val description = stringResource(id = R.string.delete_account_email_verification_description)
                                val btnText = stringResource(id = R.string.send_one_time_passcode)

                                SettingsItem(
                                    title = stringResource(id = R.string.delete_account),
                                    type = SettingsItemType.None,
                                    onClick = {
                                        navController?.navigateSafely(
                                            AccountSettingsFragmentDirections.actionAccountSettingsFragmentToConfirmationDialogFragment(
                                                confirmation = confirmation,
                                                description = description,
                                                btnText = btnText,
                                                title = title,
                                                key = Constants.IsConfirmed,
                                            )
                                        )
                                    },
                                    titleColor = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        onFailure = {
                            val text = stringResource(id = R.string.something_went_wrong)
                            scope.launch { snackbarHostState.showSnackbar(text) }
                        },
                    )
                }

                LaunchedEffect(isRequestConfirmed, userState) {
                    userState.fold(
                        onSuccess = { user ->
                            if (isRequestConfirmed) {
                                navController?.navigateSafely(
                                    AccountSettingsFragmentDirections.actionAccountSettingsFragmentToVerifyOtpDialogFragment(
                                        email = user.email,
                                        title = context.stringResource(R.string.confirm_one_time_passcode),
                                        primaryButtonText = context.stringResource(R.string.confirm),
                                        secondaryButtonText = context.stringResource(R.string.cancel),
                                        progressIndicatorText = context.stringResource(R.string.confirming_one_time_passcode),
                                        type = OtpType.DeleteAccount,
                                        destinationId = R.id.accountSettingsFragment,
                                    )
                                )
                            }
                        }
                    )
                }

                val confirmationText = stringResource(id = R.string.delete_account_confirmation)
                val descriptionText = stringResource(id = R.string.delete_account_description)
                val deleteText = stringResource(id = R.string.delete_account)

                LaunchedEffect(isOtpVerified) {
                    if (isOtpVerified == true) {
                        navController?.navigateSafely(
                            AccountSettingsFragmentDirections.actionAccountSettingsFragmentToConfirmationDialogFragment(
                                confirmation = confirmationText,
                                description = descriptionText,
                                btnText = deleteText,
                                key = Constants.DeleteAccount,
                            )
                        )
                    } else {
                        // TODO
                    }
                }
            }
        }
    }

    private fun logOut() {
        navController?.popBackStack()
        context?.let { context ->
            navController?.navigateSafely(
                AccountSettingsFragmentDirections.actionAccountSettingsFragmentToProgressIndicatorDialogFragment(
                    context.stringResource(R.string.loggin_out)
                )
            )
        }

        viewModel.logOutUser().invokeOnCompletion {
            navController?.popBackStack()
            navController?.navigateSafely(AccountSettingsFragmentDirections.actionAccountSettingsFragmentToStartFragment())
        }
    }

    private fun deleteUser() {
        navController?.popBackStack()
        context?.let { context ->
            navController?.navigateSafely(
                AccountSettingsFragmentDirections.actionAccountSettingsFragmentToProgressIndicatorDialogFragment(
                    context.stringResource(R.string.deleting_account)
                )
            )
        }
        viewModel.deleteUser().invokeOnCompletion {
            navController?.popBackStack()
            navController?.navigateSafely(AccountSettingsFragmentDirections.actionAccountSettingsFragmentToStartFragment())
        }
    }
}