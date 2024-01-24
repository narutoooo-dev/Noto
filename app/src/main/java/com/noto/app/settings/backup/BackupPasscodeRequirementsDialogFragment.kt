package com.noto.app.settings.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.fragment.navArgs
import com.noto.app.R
import com.noto.app.components.dialog.BaseDialogFragment
import com.noto.app.components.dialog.BottomSheetDialog
import com.noto.app.components.util.Group
import com.noto.app.settings.SettingsItem
import com.noto.app.settings.SettingsItemType
import com.noto.app.util.Constants

class BackupPasscodeRequirementsDialogFragment : BaseDialogFragment() {

    private val args by navArgs<BackupPasscodeRequirementsDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                BottomSheetDialog(title = stringResource(id = R.string.backup_passcode_requirements)) {
                    Group(modifier = Modifier.fillMaxWidth()) {
                        BackupPasscodeRequirements.entries.forEach { requirement ->
                            val isValid = remember(requirement) { args.passcode.matches(requirement.regex) }
                            SettingsItem(
                                title = stringResource(id = requirement.titleStringResourceId),
                                type = SettingsItemType.None,
                                painter = if (isValid) painterResource(id = R.drawable.ic_round_check_24) else painterResource(id = R.drawable.ic_round_cancel_24),
                                painterColor = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class BackupPasscodeRequirements(val titleStringResourceId: Int, val regex: Regex) {
    MinChars(R.string.password_requirement_min_chars, Constants.Regex.MinChars),
    NumberChars(R.string.password_requirement_number_chars, Constants.Regex.NumberChars),
}