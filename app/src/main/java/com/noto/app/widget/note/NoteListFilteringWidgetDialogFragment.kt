package com.noto.app.widget.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.text.toSpannable
import com.noto.app.R
import com.noto.app.domain.FilteringType
import com.noto.app.ui.component.dialog.BaseDialogFragment
import com.noto.app.ui.component.dialog.BottomSheetDialog
import com.noto.app.ui.component.dialog.SelectableDialogItem
import com.noto.app.ui.component.material.MediumSubtitle
import com.noto.app.ui.theme.NotoTheme
import com.noto.app.ui.util.toAnnotatedString
import com.noto.app.ui.util.toDescriptionResourceId
import com.noto.app.ui.util.toStringResourceId

class NoteListFilteringWidgetDialogFragment() : BaseDialogFragment() {
    private var onClick: (FilteringType) -> Unit = {}
    private var filteringType: FilteringType? = null

    constructor(filteringType: FilteringType, onClick: (FilteringType) -> Unit) : this() {
        this.filteringType = filteringType
        this.onClick = onClick
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = context?.let { context ->
        ComposeView(context).apply {
            setContent {
                val types = FilteringType.entries

                BottomSheetDialog(title = stringResource(R.string.filtering)) {
                    types.forEach { type ->
                        val typeDescription = remember(type) {
                            context.getText(type.toDescriptionResourceId()).toSpannable().toAnnotatedString()
                        }

                        SelectableDialogItem(
                            selected = filteringType == type,
                            onClick = {
                                onClick(type)
                                dismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(NotoTheme.dimensions.extraSmall)) {
                                Text(text = stringResource(id = type.toStringResourceId()))
                                MediumSubtitle(text = typeDescription)
                            }
                        }
                    }
                }
            }
        }
    }
}