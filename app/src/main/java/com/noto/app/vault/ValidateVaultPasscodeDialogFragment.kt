package com.noto.app.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.domain.model.NotoException
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.util.Constants
import com.noto.app.util.navController
import com.noto.app.util.stringResource
import org.koin.androidx.viewmodel.ext.android.viewModel

class ValidateVaultPasscodeDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<VaultPasscodeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->

        val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.stringResource(R.string.validate))
            .setNegativeButtonText(context.stringResource(R.string.use_passcode))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    passcodeIsValid()
                }
            }
        )

        ComposeView(context).apply {
            setContent {
                val state by viewModel.state.collectAsState()
                val isBioAuthEnabled by viewModel.isBioAuthEnabled.collectAsState()
                val vaultPasscode by viewModel.vaultPasscode.collectAsState()
                val vaultPasscodeStatus by viewModel.vaultPasscodeStatus.collectAsState()
                val focusRequester = remember { FocusRequester() }

                BottomSheetDialog(title = stringResource(id = R.string.enter_vault_passcode)) {

                    NotoPasswordTextField(
                        value = vaultPasscode,
                        onValueChange = viewModel::setVaultPasscode,
                        placeholder = stringResource(id = R.string.passcode),
                        status = vaultPasscodeStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    LaunchedEffect(Unit) {
                        if (!isBioAuthEnabled) focusRequester.requestFocus()
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                    NotoButton(
                        text = stringResource(id = R.string.validate),
                        onClick = viewModel::validatePasscode,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    if (isBioAuthEnabled) {
                        NotoTextButton(
                            text = stringResource(id = R.string.use_bio),
                            onClick = { biometricPrompt.authenticate(biometricPromptInfo) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        LaunchedEffect(Unit) {
                            biometricPrompt.authenticate(biometricPromptInfo)
                        }
                    }
                }

                LaunchedEffect(state) {
                    state.fold(
                        onSuccess = { passcodeIsValid() },
                        onFailure = { exception ->
                            when (exception) {
                                NotoException.Vault.PasscodeIsRequired -> {
                                    viewModel.setVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_is_required))
                                }

                                NotoException.Vault.InvalidPasscode -> {
                                    viewModel.setVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_is_invalid))
                                }

                                else -> {}
                            }
                        }
                    )
                }
            }
        }
    }

    private fun passcodeIsValid() {
        navController?.previousBackStackEntry?.savedStateHandle?.set(Constants.IsPasscodeValid, true)
    }

}