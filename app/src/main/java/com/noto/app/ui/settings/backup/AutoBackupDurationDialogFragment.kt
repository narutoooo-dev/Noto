package com.noto.app.ui.settings.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import com.noto.app.R
import com.noto.app.domain.AutoBackupDuration
import com.noto.app.ui.component.dialog.BaseDialogFragment
import com.noto.app.ui.component.dialog.BottomSheetDialog
import com.noto.app.ui.component.dialog.SelectableDialogItem
import com.noto.app.ui.util.toStringResourceId
import org.koin.androidx.viewmodel.ext.android.viewModel

class AutoBackupDurationDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<LocalBackupSettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val options = AutoBackupDuration.entries
                val selectedOption by viewModel.autoBackupDuration.collectAsState()
                BottomSheetDialog(title = stringResource(id = R.string.auto_backup_duration)) {
                    options.forEach { option ->
                        SelectableDialogItem(
                            selected = selectedOption == option,
                            onClick = {
                                viewModel.updateAutoBackupDuration(option)
                                dismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(id = option.toStringResourceId()))
                        }
                    }
                }
            }
        }
    }
}