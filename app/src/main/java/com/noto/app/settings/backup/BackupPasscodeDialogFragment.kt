package com.noto.app.settings.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.toSpannable
import com.noto.app.R
import com.noto.app.components.dialog.BaseDialogFragment
import com.noto.app.components.dialog.BottomSheetDialog
import com.noto.app.components.material.NotoButton
import com.noto.app.components.material.NotoPasswordTextField
import com.noto.app.components.material.Password
import com.noto.app.fold
import com.noto.app.theme.NotoTheme
import com.noto.app.util.Constants
import com.noto.app.util.navController
import com.noto.app.util.navigateSafely
import com.noto.app.util.toAnnotatedString
import org.koin.androidx.viewmodel.ext.android.viewModel

class BackupPasscodeDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<LocalBackupSettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val passcode by viewModel.backupPasscode.collectAsState()
                val confirmPasscode by viewModel.backupConfirmPasscode.collectAsState()
                val passcodeStatus by viewModel.backupPasscodeStatus.collectAsState()
                val confirmPasscodeStatus by viewModel.backupConfirmPasscodeStatus.collectAsState()
                val passcodeState by viewModel.backupPasscodeState.collectAsState()
                val passcodeRequirementsAnnotatedString = remember {
                    context.getText(R.string.backup_passcode_requirements).toSpannable().toAnnotatedString()
                }
                val isPasscodeVisible = rememberSaveable { mutableStateOf(false) }
                val passcodeKeyboardOptions = remember { KeyboardOptions.Password.copy(imeAction = ImeAction.Next) }
                val confirmPasscodeKeyboardOptions = remember { KeyboardOptions.Password.copy(imeAction = ImeAction.Done) }

                BottomSheetDialog(title = stringResource(id = R.string.backup_passcode)) {
                    Surface(shape = MaterialTheme.shapes.small) {
                        Column(
                            modifier = Modifier.padding(NotoTheme.dimensions.medium),
                            verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                        ) {
                            Text(
                                text = stringResource(id = R.string.backup_passcode_title),
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(id = R.string.backup_passcode_description),
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    NotoPasswordTextField(
                        value = passcode,
                        onValueChange = viewModel::setBackupPasscode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState -> if (!focusState.isFocused) viewModel.validatePasscode() },
                        placeholder = stringResource(id = R.string.passcode),
                        status = passcodeStatus,
                        isPasswordVisible = isPasscodeVisible,
                        keyboardOptions = passcodeKeyboardOptions,
                    )

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    NotoPasswordTextField(
                        value = confirmPasscode,
                        onValueChange = viewModel::setBackupConfirmPasscode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState -> if (!focusState.isFocused) viewModel.validateConfirmPasscode() },
                        placeholder = stringResource(id = R.string.confirm_passcode),
                        status = confirmPasscodeStatus,
                        isPasswordVisible = isPasscodeVisible,
                        keyboardOptions = confirmPasscodeKeyboardOptions,
                    )

                    Spacer(Modifier.height(NotoTheme.dimensions.medium))

                    ClickableText(
                        text = passcodeRequirementsAnnotatedString,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline, textAlign = TextAlign.End),
                        onClick = {
                            navController?.navigateSafely(
                                BackupPasscodeDialogFragmentDirections.actionBackupPasscodeDialogFragmentToBackupPasscodeRequirementsDialogFragment(
                                    passcode
                                )
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.extraLarge))

                    NotoButton(
                        text = stringResource(id = R.string.confirm),
                        onClick = viewModel::confirmBackupPasscode,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                LaunchedEffect(passcodeState) {
                    passcodeState.fold(
                        onSuccess = {
                            navController?.previousBackStackEntry?.savedStateHandle?.set(Constants.BackupPasscode, passcode)
                            dismiss()
                        },
                    )
                }
            }
        }
    }

}