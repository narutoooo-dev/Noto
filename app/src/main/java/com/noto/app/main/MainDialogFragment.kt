package com.noto.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.noto.app.R
import com.noto.app.components.BaseDialogFragment
import com.noto.app.components.BottomSheetDialog
import com.noto.app.components.BottomSheetDialogItem
import com.noto.app.theme.NotoTheme
import com.noto.app.util.Constants
import com.noto.app.util.navController
import com.noto.app.util.navigateSafely
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainDialogFragment : BaseDialogFragment() {

    private val viewModel by sharedViewModel<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->

        navController?.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean?>(Constants.IsPasscodeValid)
            ?.run {
                observe(viewLifecycleOwner) { isPasscodeValid ->
                    if (isPasscodeValid == true) {
                        viewModel.openVault()
                        val currentDestinationId = navController?.currentDestination?.id
                        val isValidateDialog = currentDestinationId == R.id.validateVaultPasscodeDialogFragment
                        val isVaultDialog = currentDestinationId == R.id.vaultPasscodeDialogFragment
                        if (isValidateDialog || isVaultDialog) navController?.navigateUp()
                        navController?.navigateSafely(MainDialogFragmentDirections.actionMainDialogFragmentToMainVaultFragment())
                        value = null
                    }
                }
            }

        ComposeView(context).apply {
            setContent {
                val vaultPasscode by viewModel.vaultPasscode.collectAsState()
                val isVaultOpen by viewModel.isVaultOpen.collectAsState()

                BottomSheetDialog(title = stringResource(R.string.app_name)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium)) {
                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.folders_vault),
                            onClick = {
                                when {
                                    vaultPasscode == null -> navController?.navigateSafely(MainDialogFragmentDirections.actionMainDialogFragmentToVaultPasscodeDialogFragment())
                                    isVaultOpen -> navController?.navigateSafely(MainDialogFragmentDirections.actionMainDialogFragmentToMainVaultFragment())
                                    else -> navController?.navigateSafely(MainDialogFragmentDirections.actionMainDialogFragmentToValidateVaultPasscodeDialogFragment())
                                }
                            },
                            painter = painterResource(id = R.drawable.ic_round_shield_24),
                        )
                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.folders_archive),
                            onClick = {
                                navController?.navigateSafely(MainDialogFragmentDirections.actionMainDialogFragmentToMainArchiveFragment())
                            },
                            painter = painterResource(id = R.drawable.ic_round_inventory_24),
                        )
                    }

                    Spacer(modifier = Modifier.height(NotoTheme.dimensions.medium))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.medium)) {
                        BottomSheetDialogItem(
                            text = stringResource(id = R.string.settings),
                            onClick = {
                                navController?.navigateSafely(MainDialogFragmentDirections.actionMainDialogFragmentToSettingsFragment())
                            },
                            painter = painterResource(id = R.drawable.ic_round_settings_24),
                        )
                    }
                }
            }
        }
    }
}