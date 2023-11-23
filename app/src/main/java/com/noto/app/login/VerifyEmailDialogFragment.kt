package com.noto.app.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.fragment.navArgs
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VerifyEmailDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<LoginViewModel>()

    private val args by navArgs<VerifyEmailDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)

        ComposeView(context).apply {
            setContent {
                val state by viewModel.state.collectAsState()
                val otp by viewModel.otp.collectAsState()
                val otpStatus by viewModel.otpStatus.collectAsState()
                val focusRequester = remember { FocusRequester() }

                BottomSheetDialog(title = stringResource(id = R.string.verify_email)) {
                    Text(
                        text = stringResource(id = R.string.verify_email_info),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .surface(),
                    )

                    Spacer(Modifier.height(NotoTheme.dimensions.medium))

                    NotoTextField(
                        value = otp,
                        onValueChange = viewModel::setOtp,
                        placeholder = stringResource(id = R.string.one_time_passcode),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_key_24),
                                contentDescription = stringResource(id = R.string.one_time_passcode)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        status = otpStatus,
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    Spacer(Modifier.height(NotoTheme.dimensions.extraLarge))

                    Button(
                        text = stringResource(id = R.string.verify_email),
                        onClick = {
                            if (otp.isNotBlank()) {
                                viewModel.verifyEmail(args.email, otp)
                            } else {
                                viewModel.setOtpStatus(TextFieldStatus.Error(R.string.one_time_passcode_is_required))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(NotoTheme.dimensions.medium))

                    TextButton(
                        text = stringResource(id = R.string.edit_email),
                        onClick = { dismiss() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                    )
                }


                state.fold(
                    onLoading = {
                        navController?.navigateSafely(
                            VerifyEmailDialogFragmentDirections.actionGlobalProgressIndicatorDialogFragment(
                                stringResource(id = R.string.verifying_email)
                            )
                        )
                    },
                    onSuccess = {
                        SideEffect {
                            navController?.popBackStack(R.id.verifyEmailDialogFragment, true)
                            if (navController?.currentDestination?.id == R.id.changeEmailDialogFragment) {
                                navController?.navigateUp()
                                parentFragment?.view?.snackbar(context.stringResource(id = R.string.email_is_updated))
                            }
                        }
                    },
                    onFailure = {
                        if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                            navController?.navigateUp()

                        viewModel.setOtpStatus(TextFieldStatus.Error(R.string.one_time_passcode_is_invalid))
                    },
                )
            }
        }
    }
}