package com.noto.app.settings.vault

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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.noto.app.util.snackbar
import com.noto.app.util.stringResource
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeVaultPasscodeDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<VaultSettingsViewModel>()

    private val parentView by lazy { parentFragment?.view }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val state by viewModel.vaultPasscodeState.collectAsState()
                val currentVaultPasscode by viewModel.currentVaultPasscode.collectAsState()
                val currentVaultPasscodeStatus by viewModel.currentVaultPasscodeStatus.collectAsState()
                val newVaultPasscode by viewModel.newVaultPasscode.collectAsState()
                val newVaultPasscodeStatus by viewModel.newVaultPasscodeStatus.collectAsState()
                val currentVaultPasscodeFocusRequester = remember { FocusRequester() }
                val newVaultPasscodeFocusRequester = remember { FocusRequester() }
                val vaultPasscodeMessage = stringResource(R.string.vault_passcode_message)
                val currentVaultPasscodeKeyboardOptions = remember { KeyboardOptions(imeAction = ImeAction.Next) }
                val newVaultPasscodeKeyboardOptions = remember { KeyboardOptions(imeAction = ImeAction.Done) }
                val focusManager = LocalFocusManager.current
                val isPasscodeVisible = rememberSaveable { mutableStateOf(false) }

                BottomSheetDialog(title = stringResource(id = R.string.change_passcode)) {

                    Surface {
                        Text(
                            text = vaultPasscodeMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NotoTheme.dimensions.medium),
                        )
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    NotoPasswordTextField(
                        value = currentVaultPasscode,
                        onValueChange = viewModel::setCurrentVaultPasscode,
                        placeholder = stringResource(id = R.string.current_passcode),
                        status = currentVaultPasscodeStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(currentVaultPasscodeFocusRequester),
                        keyboardOptions = currentVaultPasscodeKeyboardOptions,
                        isPasswordVisible = isPasscodeVisible,
                    )

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    NotoPasswordTextField(
                        value = newVaultPasscode,
                        onValueChange = viewModel::setNewVaultPasscode,
                        placeholder = stringResource(id = R.string.new_passcode),
                        status = newVaultPasscodeStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(newVaultPasscodeFocusRequester),
                        keyboardOptions = newVaultPasscodeKeyboardOptions,
                        isPasswordVisible = isPasscodeVisible,
                    )

                    LaunchedEffect(Unit) {
                        currentVaultPasscodeFocusRequester.requestFocus()
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                    NotoButton(
                        text = stringResource(id = R.string.update_passcode),
                        onClick = viewModel::updateVaultPasscode,
                        modifier = Modifier.fillMaxWidth(),
                    )

                }

                LaunchedEffect(state) {
                    state.fold(
                        onSuccess = {
                            parentView?.snackbar(context.stringResource(R.string.vault_passcode_has_changed), R.drawable.ic_round_key_24)
                            focusManager.clearFocus()
                            dismiss()
                        },
                        onFailure = { exception ->
                            when (exception) {
                                NotoException.Vault.MismatchedPasscodes -> {
                                    viewModel.setCurrentVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_doesnt_match))
                                }

                                NotoException.Vault.PasscodeIsRequired -> {
                                    viewModel.setCurrentVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_is_required))
                                }

                                NotoException.Vault.NewPasscodeIsRequired -> {
                                    viewModel.setNewVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_is_required))
                                }

                                NotoException.Vault.PasscodeRequirements -> {
                                    viewModel.setNewVaultPasscodeStatus(TextFieldStatus.Error(R.string.passcode_length_message))
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