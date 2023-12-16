package com.noto.app.settings.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import com.noto.app.R
import com.noto.app.UiState
import com.noto.app.components.screen.Screen
import com.noto.app.domain.model.AutoBackupDuration
import com.noto.app.domain.model.NotoColor
import com.noto.app.domain.model.NotoException
import com.noto.app.settings.SettingsItem
import com.noto.app.settings.SettingsItemType
import com.noto.app.settings.SettingsSection
import com.noto.app.theme.NotoTheme
import com.noto.app.theme.toColor
import com.noto.app.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocalBackupSettingsFragment : Fragment() {

    private val viewModel by viewModel<LocalBackupSettingsViewModel>()

    private val parentView by lazy { parentFragment?.view }

    private val autoBackupLocationLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            context?.contentResolver?.takePersistableUriPermission(uri, ReadWriteUriPermissions)
            viewModel.updateAutoBackupLocation(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = context?.let { context ->
        setupMixedTransitions()
        activity?.onBackPressedDispatcher?.addCallback { navController?.navigateUp() }
        navController?.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(Constants.IsConfirmed)
            ?.observe(viewLifecycleOwner) { isConfirmed -> if (isConfirmed) autoBackupLocationLauncher.launch(Uri.EMPTY) }

        ComposeView(context).apply {
            isTransitionGroup = true
            setContent {
                val autoBackupLocation by viewModel.autoBackupLocation.collectAsState()
                val autoBackupDuration by viewModel.autoBackupDuration.collectAsState()
                val autoBackupDirectory = remember(autoBackupLocation) { autoBackupLocation?.directoryPath }
                val backUpState by viewModel.backUpState.collectAsState()
                val restoreState by viewModel.restoreState.collectAsState()
                val isAutoBackupEnabled = remember(autoBackupLocation) { autoBackupLocation != null }

                Screen(
                    title = stringResource(id = R.string.local_backup),
                    modifier = Modifier.animateContentSize(),
                ) {
                    AutoBackup(
                        autoBackupDuration = autoBackupDuration,
                        isAutoBackupEnabled = isAutoBackupEnabled,
                        autoBackupDirectory = autoBackupDirectory,
                        onToggle = {
                            if (isAutoBackupEnabled) {
                                viewModel.disableAutoBackup()
                            } else {
                                navController?.navigateSafely(
                                    LocalBackupSettingsFragmentDirections.actionLocalBackupSettingsFragmentToConfirmationDialogFragment(
                                        title = context.stringResource(R.string.auto_backup_location),
                                        confirmation = context.stringResource(R.string.auto_backup_location_confirmation),
                                        description = context.stringResource(R.string.auto_backup_location_description),
                                        btnText = context.stringResource(R.string.auto_backup_location_btn_text),
                                        isError = false,
                                    )
                                )
                            }
                        },
                        onBackUp = viewModel::backUp,
                        onRestore = viewModel::restore,
                    )
                    if (isAutoBackupEnabled) Spacer(modifier = Modifier.weight(1F))
                    ManualBackup()
                }
                BackUpStateEffect(backUpState, context)
                RestoreStateEffect(restoreState, context)
            }
        }
    }

    @Composable
    private fun AutoBackup(
        autoBackupDuration: AutoBackupDuration,
        isAutoBackupEnabled: Boolean,
        autoBackupDirectory: String?,
        onToggle: () -> Unit,
        onBackUp: () -> Unit,
        onRestore: () -> Unit,
    ) {
        SettingsSection {
            SettingsItem(
                title = stringResource(id = R.string.auto_backup),
                type = SettingsItemType.Switch(isAutoBackupEnabled),
                onClick = onToggle,
                painter = painterResource(id = R.drawable.ic_round_auto_backup_24),
            )

            AnimatedVisibility(visible = isAutoBackupEnabled) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium),
                ) {
                    SettingsSection {
                        SettingsItem(
                            title = stringResource(id = R.string.auto_backup_location),
                            type = SettingsItemType.Text(autoBackupDirectory ?: stringResource(id = R.string.none)),
                            painter = painterResource(id = R.drawable.ic_round_auto_backup_location_24),
                            onClick = { autoBackupLocationLauncher.launch(Uri.EMPTY) },
                        )

                        SettingsItem(
                            title = stringResource(id = R.string.auto_backup_duration),
                            type = SettingsItemType.Text(stringResource(id = autoBackupDuration.toStringResourceId())),
                            painter = painterResource(id = R.drawable.ic_round_schedule_24),
                            onClick = {
                                navController?.navigateSafely(LocalBackupSettingsFragmentDirections.actionLocalBackupSettingsFragmentToAutoBackupDurationDialogFragment())
                            }
                        )

                        SettingsItem(
                            title = stringResource(id = R.string.back_up_now),
                            type = SettingsItemType.None,
                            painter = painterResource(id = R.drawable.ic_round_reset_24),
                            onClick = onBackUp,
                            titleColor = NotoColor.Purple.toColor(),
                            painterColor = NotoColor.Purple.toColor(),
                        )

                        SettingsItem(
                            title = stringResource(R.string.restore_backup),
                            type = SettingsItemType.None,
                            painter = painterResource(id = R.drawable.ic_round_restore_backup_24),
                            onClick = onRestore,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ManualBackup() {
        SettingsSection {
            SettingsItem(
                title = stringResource(id = R.string.manual_backup),
                type = SettingsItemType.None,
                onClick = {
                    navController?.navigateSafely(LocalBackupSettingsFragmentDirections.actionLocalBackupSettingsFragmentToManualBackupDialogFragment())
                },
                painter = painterResource(id = R.drawable.ic_round_manual_backup_24),
            )
        }
    }

    @Composable
    private fun BackUpStateEffect(state: UiState<Unit>, context: Context) {
        LaunchedEffect(state) {
            when (state) {
                is UiState.Empty -> {}
                is UiState.Loading -> {
                    navController?.navigateSafely(
                        LocalBackupSettingsFragmentDirections.actionLocalBackupSettingsFragmentToProgressIndicatorDialogFragment(
                            context.stringResource(R.string.backing_up)
                        )
                    )
                }

                is UiState.Success -> {
                    parentView?.snackbar(context.stringResource(R.string.data_is_backed_up), R.drawable.ic_round_check_24)
                    if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment) {
                        navController?.navigateUp()
                    }
                }

                is UiState.Failure -> {
                    when (state.exception) {
                        NotoException.LocalBackup.Export.ExportFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.back_up_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.LocalBackup.Export.FileCreationFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.create_file_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.LocalBackup.Export.NoFolderSelected -> {
                            parentView?.snackbar(context.stringResource(R.string.no_folder_is_selected), R.drawable.ic_round_warning_24)
                        }

                        else -> {
                            parentView?.snackbar(context.stringResource(R.string.something_went_wrong), R.drawable.ic_round_error_24)
                        }
                    }
                    if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment) {
                        navController?.navigateUp()
                    }
                }
            }
        }
    }

    @Composable
    private fun RestoreStateEffect(state: UiState<Unit>, context: Context) {
        LaunchedEffect(state) {
            when (state) {
                is UiState.Empty -> {}
                is UiState.Loading -> {
                    navController?.navigateSafely(
                        LocalBackupSettingsFragmentDirections.actionLocalBackupSettingsFragmentToProgressIndicatorDialogFragment(
                            context.stringResource(R.string.restoring)
                        )
                    )
                }

                is UiState.Success -> {
                    parentView?.snackbar(context.stringResource(id = R.string.data_is_restored), R.drawable.ic_round_check_24)
                    if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment) {
                        navController?.navigateUp()
                    }
                }

                is UiState.Failure -> {
                    when (state.exception) {
                        NotoException.LocalBackup.Import.ImportFailed -> {
                            parentView?.snackbar(context.stringResource(R.string.restore_failed), R.drawable.ic_round_error_24)
                        }

                        NotoException.LocalBackup.Import.NoFileSelected -> {
                            parentView?.snackbar(context.stringResource(R.string.no_file_is_selected), R.drawable.ic_round_warning_24)
                        }

                        else -> {
                            parentView?.snackbar(context.stringResource(R.string.something_went_wrong), R.drawable.ic_round_error_24)
                        }
                    }
                    if (navController?.currentDestination?.id == R.id.progressIndicatorDialogFragment) {
                        navController?.navigateUp()
                    }
                }
            }
        }
    }

    companion object {
        private const val ReadWriteUriPermissions = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }

}