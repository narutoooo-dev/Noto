package com.noto.app.label

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.navigation.fragment.navArgs
import com.noto.app.R
import com.noto.app.components.*
import com.noto.app.domain.model.NotoException
import com.noto.app.fold
import com.noto.app.theme.toColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NewLabelDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<LabelViewModel> { parametersOf(args.folderId, args.labelId) }

    private val args by navArgs<NewLabelDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val folder by viewModel.folder.collectAsState()
                val label by viewModel.label.collectAsState()
                val titleStatus by viewModel.titleStatus.collectAsState()
                val state by viewModel.state.collectAsState()
                val keyboardOptions = remember { KeyboardOptions(imeAction = ImeAction.Done) }
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                BottomSheetDialog(
                    title = if (args.labelId == 0L) stringResource(id = R.string.new_label) else stringResource(id = R.string.edit_label),
                    headerColor = folder.color.toColor(),
                ) {
                    NotoTextField(
                        value = label.title,
                        onValueChange = viewModel::setTitle,
                        placeholder = stringResource(id = R.string.title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = keyboardOptions,
                        status = titleStatus,
                    )

                    Button(
                        text = if (args.labelId == 0L) stringResource(id = R.string.create_label) else stringResource(id = R.string.update_label),
                        onClick = viewModel::createOrUpdateLabel,
                        containerColor = folder.color.toColor(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                LaunchedEffect(state) {
                    state.fold(
                        onSuccess = { dismiss() },
                        onFailure = { exception ->
                            when (exception) {
                                NotoException.Model.TitleIsRequired -> {
                                    focusManager.clearFocus()
                                    focusRequester.requestFocus()
                                    viewModel.setTitleStatus(TextFieldStatus.Error(R.string.title_is_required))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}