package com.noto.app.settings.backup

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.noto.app.domain.model.BackupFormat
import com.noto.app.domain.model.NotoException
import com.noto.app.theme.NotoTheme
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManualBackupDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<LocalBackupSettingsViewModel>()

    private val parentView by lazy { parentFragment?.view }

    private val exportDataLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        viewModel.export(uri)
    }

    private val importDataLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.import(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->

        navController?.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<String>(Constants.BackupPasscode)
            ?.observe(viewLifecycleOwner) {
                viewModel.updateManualBackupFormat(BackupFormat.Encrypted)
                viewModel.updateManualBackupPasscode(it)
            }

        ComposeView(context).apply {
            setContent {
                val exportState by viewModel.exportState.collectAsState()
                val importState by viewModel.importState.collectAsState()
                val manualBackupFormat by viewModel.manualBackupFormat.collectAsState()
                BottomSheetDialog(title = stringResource(id = R.string.manual_backup)) {
                    BottomSheetDialogItem(
                        text = stringResource(id = R.string.backup_format),
                        onClick = {
                            navController?.navigateSafely(
                                ManualBackupDialogFragmentDirections.actionManualBackupDialogFragmentToBackupFormatDialogFragment(
                                    selectedFormat = manualBackupFormat,
                                )
                            )
                        },
                        painter = painterResource(id = R.drawable.ic_round_backup_format_24),
                        value = stringResource(manualBackupFormat.toStringResourceId()),
                    )

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium)) {
                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.manual_backup_export),
                            onClick = { exportDataLauncher.launch(Uri.EMPTY) },
                            painter = painterResource(id = R.drawable.ic_round_file_upload_24),
                        )

                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.manual_backup_import),
                            onClick = { importDataLauncher.launch(LocalBackupHandler.FileTypes) },
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
                        ManualBackupDialogFragmentDirections.actionManualBackupDialogFragmentToProgressIndicatorDialogFragment(
                            context.stringResource(R.string.exporting_data)
                        )
                    )
                }

                is UiState.Failure -> {
                    when (state.exception) {
                        NotoException.LocalBackup.Export.ExportFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.exporting_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.LocalBackup.Export.FileCreationFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.create_file_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.LocalBackup.Export.NoFolderSelected -> {
                            parentView?.snackbar(context.stringResource(R.string.no_folder_is_selected), R.drawable.ic_round_warning_24)
                        }

                        else -> {
                            state.exception.printStackTrace()
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
                        ManualBackupDialogFragmentDirections.actionManualBackupDialogFragmentToProgressIndicatorDialogFragment(
                            context.stringResource(R.string.importing_data)
                        )
                    )
                }

                is UiState.Failure -> {
                    when (state.exception) {
                        NotoException.LocalBackup.Import.ImportFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.importing_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.LocalBackup.Import.NoFileSelected -> {
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