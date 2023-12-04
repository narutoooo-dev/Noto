package com.noto.app.login

import android.annotation.SuppressLint
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
import com.noto.app.components.dialog.BaseDialogFragment
import com.noto.app.components.dialog.BottomSheetDialog
import com.noto.app.components.material.NotoButton
import com.noto.app.components.material.NotoTextButton
import com.noto.app.components.material.NotoTextField
import com.noto.app.components.material.TextFieldStatus
import com.noto.app.domain.model.NotoException
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.util.Constants
import com.noto.app.util.navController
import com.noto.app.util.navigateSafely
import com.noto.app.util.surface
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

@SuppressLint("RestrictedApi")
class VerifyOtpDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<VerifyOtpViewModel> { parametersOf(args.email, args.type, args.sendOtp) }

    private val args by navArgs<VerifyOtpDialogFragmentArgs>()

    private val navBackStackEntry by lazy {
        navController?.currentBackStack?.value?.lastOrNull { it.destination.id == args.destinationId }
    }

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

                BottomSheetDialog(title = args.title) {
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

                    NotoButton(
                        text = args.primaryButtonText,
                        onClick = viewModel::verifyOtp,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(NotoTheme.dimensions.medium))

                    NotoTextButton(
                        text = args.secondaryButtonText,
                        onClick = ::dismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                    )
                }

                LaunchedEffect(state) {
                    state.fold(
                        onLoading = {
                            navController?.navigateSafely(
                                VerifyOtpDialogFragmentDirections.actionGlobalProgressIndicatorDialogFragment(args.progressIndicatorText)
                            )
                        },
                        onSuccess = {
                            navBackStackEntry?.savedStateHandle?.set(Constants.VerifyOtp, true)
                            navController?.popBackStack(R.id.verifyOtpDialogFragment, true)
                        },
                        onFailure = { exception ->
                            if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment)
                                navController?.navigateUp()
                            when (exception) {
                                is NotoException.Auth.InvalidOtp -> {
                                    viewModel.setOtpStatus(TextFieldStatus.Error(R.string.one_time_passcode_is_invalid))
                                }

                                else -> {
                                    // TODO
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}