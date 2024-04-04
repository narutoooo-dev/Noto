package com.noto.app.ui.settings.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.noto.app.R
import com.noto.app.domain.NotoException
import com.noto.app.domain.OtpType
import com.noto.app.ui.component.dialog.BaseDialogFragment
import com.noto.app.ui.component.dialog.BottomSheetDialog
import com.noto.app.ui.component.material.NotoButton
import com.noto.app.ui.component.material.NotoTextField
import com.noto.app.ui.component.material.TextFieldStatus
import com.noto.app.ui.fold
import com.noto.app.ui.theme.NotoTheme
import com.noto.app.ui.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeEmailDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<AccountSettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val email by viewModel.email.collectAsState()
                val emailStatus by viewModel.emailStatus.collectAsState()
                val emailState by viewModel.emailState.collectAsState()
                val isOtpVerified by remember {
                    navController?.currentBackStackEntry?.savedStateHandle
                        ?.getStateFlow<Boolean?>(Constants.VerifyOtp, null) ?: MutableStateFlow(null)
                }.collectAsState()

                BottomSheetDialog(title = stringResource(id = R.string.change_email)) {
                    NotoTextField(
                        value = email,
                        onValueChange = {
                            viewModel.setEmail(it)
                            viewModel.setEmailStatus(TextFieldStatus.Empty)
                        },
                        placeholder = stringResource(id = R.string.email),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_email_24),
                                contentDescription = stringResource(id = R.string.email)
                            )
                        },
                        status = emailStatus,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    NotoButton(
                        text = stringResource(id = R.string.update_email),
                        onClick = viewModel::updateEmail,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                LaunchedEffect(emailState) {
                    emailState.fold(
                        onEmpty = {},
                        onLoading = {
                            navController?.navigateSafely(
                                ChangeEmailDialogFragmentDirections.actionChangeEmailDialogFragmentToProgressIndicatorDialogFragment(
                                    context.stringResource(id = R.string.updating_email)
                                )
                            )
                        },
                        onSuccess = {
                            if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                                navController?.navigateUp()

                            navController?.navigateSafely(
                                ChangeEmailDialogFragmentDirections.actionChangeEmailDialogFragmentToVerifyOtpDialogFragment(
                                    email = email,
                                    title = context.stringResource(id = R.string.verify_email),
                                    primaryButtonText = context.stringResource(id = R.string.verify_email),
                                    secondaryButtonText = context.stringResource(id = R.string.edit_email),
                                    progressIndicatorText = context.stringResource(id = R.string.verifying_email),
                                    type = OtpType.ChangeEmail,
                                    destinationId = R.id.changeEmailDialogFragment,
                                    sendOtp = false,
                                )
                            )
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

                                else -> {
                                    navController?.navigateUp()
                                    parentFragment?.view?.snackbar(context.stringResource(id = R.string.something_went_wrong))
                                }
                            }
                        }
                    )
                }

                LaunchedEffect(isOtpVerified) {
                    if (isOtpVerified == true) {
                        navController?.popBackStack(R.id.changeEmailDialogFragment, true)
                        parentFragment?.view?.snackbar(context.stringResource(id = R.string.email_is_updated))
                    } else {
                        // TODO
                    }
                }
            }
        }
    }

}