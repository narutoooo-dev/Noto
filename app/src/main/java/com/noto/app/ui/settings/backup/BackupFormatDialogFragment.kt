package com.noto.app.ui.settings.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.navigation.fragment.navArgs
import com.noto.app.R
import com.noto.app.domain.BackupFormat
import com.noto.app.ui.component.dialog.BaseDialogFragment
import com.noto.app.ui.component.dialog.BottomSheetDialog
import com.noto.app.ui.component.dialog.SelectableDialogItem
import com.noto.app.ui.util.navController
import com.noto.app.ui.util.navigateSafely
import com.noto.app.ui.util.toStringResourceId

class BackupFormatDialogFragment : BaseDialogFragment() {

    private val args by navArgs<BackupFormatDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val formats = BackupFormat.entries
                BottomSheetDialog(title = stringResource(id = R.string.backup_format)) {
                    formats.forEach { format ->
                        SelectableDialogItem(
                            selected = args.selectedFormat == format,
                            onClick = {
                                when (format) {
                                    BackupFormat.PlainText -> navController?.navigateUp()
                                    BackupFormat.Encrypted -> navController?.navigateSafely(BackupFormatDialogFragmentDirections.actionBackupFormatDialogFragmentToBackupPasscodeDialogFragment()) {
                                        popUpTo(R.id.backupFormatDialogFragment) {
                                            inclusive = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(id = format.toStringResourceId()))
                        }
                    }
                }
            }
        }
    }
}