package com.noto.app.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.dialog.BaseDialogFragment
import com.noto.app.components.dialog.BottomSheetDialog
import com.noto.app.components.dialog.BottomSheetDialogItem
import com.noto.app.domain.model.NotoException
import com.noto.app.theme.NotoTheme
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExportImportDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<SettingsViewModel>()

    private val parentView by lazy { parentFragment?.view }

    private val exportDataLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        viewModel.exportData(uri)
    }

    private val importDataLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.importData(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val exportState by viewModel.exportState.collectAsState()
                val importState by viewModel.importState.collectAsState()
                BottomSheetDialog(title = stringResource(id = R.string.export_import_data)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium)) {
                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.export_data),
                            onClick = { exportDataLauncher.launch(Uri.EMPTY) },
                            painter = painterResource(id = R.drawable.ic_round_file_upload_24),
                        )

                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.import_data),
                            onClick = { importDataLauncher.launch(SettingsViewModel.FileTypes) },
                            painter = painterResource(id = R.drawable.ic_round_file_download_24),
                        )
                    }
                }
                ExportStateEffect(exportState, context)
                ImportStateEffect(importState, context)
            }
        }
    }

    @Composable
    private fun ExportStateEffect(state: UiState<Uri>, context: Context) {
        LaunchedEffect(state) {
            when (state) {
                is UiState.Empty -> {}
                is UiState.Loading -> {
                    navController?.navigateSafely(
                        ExportImportDialogFragmentDirections.actionExportImportDialogFragmentToProgressIndicatorDialogFragment(
                            context.stringResource(R.string.exporting_data)
                        )
                    )
                }

                is UiState.Failure -> {
                    when (state.exception) {
                        NotoException.Export.ExportFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.exporting_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.Export.FileCreationFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.create_file_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.Export.NoFolderSelected -> {
                            parentView?.snackbar(context.stringResource(R.string.no_folder_is_selected), R.drawable.ic_round_warning_24)
                        }

                        else -> {
                            parentView?.snackbar(context.stringResource(R.string.something_went_wrong), R.drawable.ic_round_error_24)
                        }
                    }
                    navController?.navigateUp()
                    dismiss()
                }

                is UiState.Success -> {
                    parentView?.snackbar(
                        context.stringResource(R.string.data_is_exported, state.value.directoryPath),
                        R.drawable.ic_round_check_24,
                    )
                    navController?.navigateUp()
                    dismiss()
                }
            }
        }
    }

    @Composable
    private fun ImportStateEffect(state: UiState<Unit>, context: Context) {
        LaunchedEffect(state) {
            when (state) {
                is UiState.Empty -> {}
                is UiState.Loading -> {
                    navController?.navigateSafely(
                        ExportImportDialogFragmentDirections.actionExportImportDialogFragmentToProgressIndicatorDialogFragment(
                            context.stringResource(R.string.importing_data)
                        )
                    )
                }

                is UiState.Failure -> {
                    when (state.exception) {
                        NotoException.Import.ImportFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.importing_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.Import.NoFileSelected -> {
                            parentView?.snackbar(context.stringResource(R.string.no_file_is_selected), R.drawable.ic_round_warning_24)
                        }

                        else -> {
                            parentView?.snackbar(context.stringResource(R.string.something_went_wrong), R.drawable.ic_round_error_24)
                        }
                    }
                    navController?.navigateUp()
                    dismiss()
                }

                is UiState.Success -> {
                    parentView?.snackbar(context.stringResource(id = R.string.data_is_imported), R.drawable.ic_round_check_24)
                    navController?.navigateUp()
                    dismiss()
                }
            }
        }
    }
}