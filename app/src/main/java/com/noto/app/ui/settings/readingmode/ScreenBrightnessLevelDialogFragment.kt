package com.noto.app.ui.settings.readingmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import com.noto.app.R
import com.noto.app.domain.ScreenBrightnessLevel
import com.noto.app.ui.component.dialog.BaseDialogFragment
import com.noto.app.ui.component.dialog.BottomSheetDialog
import com.noto.app.ui.component.dialog.SelectableDialogItem
import com.noto.app.ui.util.toStringResourceId
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat

class ScreenBrightnessLevelDialogFragment : BaseDialogFragment() {

    private val viewModel by viewModel<ReadingModeSettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val levels = ScreenBrightnessLevel.entries
                val selectedLevel by viewModel.screenBrightnessLevel.collectAsState()
                val percentageFormatter = remember { NumberFormat.getPercentInstance() }
                BottomSheetDialog(title = stringResource(id = R.string.screen_brightness_level)) {
                    levels.forEach { level ->
                        SelectableDialogItem(
                            selected = selectedLevel == level,
                            onClick = {
                                viewModel.updateScreenBrightnessLevel(level)
                                dismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = stringResource(level.toStringResourceId()), Modifier.weight(1F))
                                if (level != ScreenBrightnessLevel.System) {
                                    val percentage = remember(level) { percentageFormatter.format(level.value) }
                                    Text(text = percentage, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}