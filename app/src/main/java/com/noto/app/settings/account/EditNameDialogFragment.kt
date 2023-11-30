package com.noto.app.settings.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.noto.app.R
import com.noto.app.components.dialog.BaseDialogFragment
import com.noto.app.components.dialog.BottomSheetDialog
import com.noto.app.components.material.NotoButton
import com.noto.app.components.material.NotoTextField
import com.noto.app.components.material.TextFieldStatus
import com.noto.app.domain.model.NotoException
import com.noto.app.fold
import com.noto.app.settings.SettingsViewModel
import com.noto.app.theme.NotoTheme
import com.noto.app.util.navController
import com.noto.app.util.navigateSafely
import com.noto.app.util.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditNameDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val name by viewModel.name.collectAsState()
                val nameStatus by viewModel.nameStatus.collectAsState()
                val nameState by viewModel.nameState.collectAsState()

                BottomSheetDialog(title = stringResource(id = R.string.edit_name)) {
                    NotoTextField(
                        value = name,
                        onValueChange = viewModel::setName,
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
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    NotoButton(
                        text = stringResource(id = R.string.update_name),
                        onClick = viewModel::updateName,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                nameState.fold(
                    onEmpty = {},
                    onLoading = {
                        navController?.navigateSafely(
                            EditNameDialogFragmentDirections.actionEditNameDialogFragmentToProgressIndicatorDialogFragment(
                                stringResource(id = R.string.updating_name)
                            )
                        )
                    },
                    onSuccess = {
                        parentFragment?.view?.snackbar(stringResource(R.string.name_is_updated))
                        SideEffect {
                            navController?.navigateUp()
                            navController?.navigateUp()
                        }
                    },
                    onFailure = { exception ->
                        when (exception) {
                            NotoException.Model.NameIsRequired -> {
                                viewModel.setNameStatus(TextFieldStatus.Error(R.string.name_is_required))
                            }

                            else -> {
                                parentFragment?.view?.snackbar(stringResource(R.string.something_went_wrong))
                                SideEffect {
                                    navController?.navigateUp()
                                    navController?.navigateUp()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}