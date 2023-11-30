package com.noto.app.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.domain.model.NotoException
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.util.Constants
import com.noto.app.util.navController
import com.noto.app.util.snackbar
import com.noto.app.util.stringResource
import org.koin.androidx.viewmodel.ext.android.viewModel

class VaultPasscodeDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<VaultPasscodeViewModel>()

    private val parentView by lazy { parentFragment?.view }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val state by viewModel.state.collectAsState()
                val vaultPasscode by viewModel.vaultPasscode.collectAsState()
                val vaultPasscodeStatus by viewModel.vaultPasscodeStatus.collectAsState()
                val vaultPasscodeKeyboardOptions = remember { KeyboardOptions(imeAction = ImeAction.Done) }
                val vaultPasscodeFocusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                val enableVaultMessage = stringResource(R.string.enable_vault_message)
                val vaultPasscodeMessage = stringResource(R.string.vault_passcode_message)
                val combinedVaultMessage = remember { enableVaultMessage.plus("\n\n").plus(vaultPasscodeMessage) }

                BottomSheetDialog(title = stringResource(id = R.string.vault_setup)) {

                    Surface {
                        Text(
                            text = combinedVaultMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NotoTheme.dimensions.medium),
                        )
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    NotoPasswordTextField(
                        value = vaultPasscode,
                        onValueChange = viewModel::setVaultPasscode,
                        placeholder = stringResource(id = R.string.new_passcode),
                        status = vaultPasscodeStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(vaultPasscodeFocusRequester),
                        keyboardOptions = vaultPasscodeKeyboardOptions,
                    )

                    LaunchedEffect(Unit) {
                        vaultPasscodeFocusRequester.requestFocus()
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                    NotoButton(
                        text = stringResource(id = R.string.enable_vault),
                        onClick = viewModel::enableVault,
                        modifier = Modifier.fillMaxWidth(),
                    )

                }

                LaunchedEffect(state) {
                    state.fold(
                        onSuccess = {
                            focusManager.clearFocus()
                            parentView?.snackbar(context.stringResource(R.string.vault_is_enabled), R.drawable.ic_round_vault_enabled_24)
                            navController?.previousBackStackEntry?.savedStateHandle?.set(Constants.IsPasscodeValid, true)
                        },
                        onFailure = { exception ->
                            when (exception) {
                                NotoException.Vault.PasscodeIsRequired -> {
                                    viewModel.setVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_is_required))
                                }

                                NotoException.Vault.PasscodeRequirements -> {
                                    viewModel.setVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_length_message))
                                }

                                else -> {}
                            }
                        },
                    )
                }
            }
        }
    }

}