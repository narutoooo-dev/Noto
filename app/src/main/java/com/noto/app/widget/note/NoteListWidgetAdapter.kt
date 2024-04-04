package com.noto.app.widget.note

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import com.noto.app.R
import com.noto.app.databinding.NoteLabelItemBinding
import com.noto.app.databinding.WidgetNoteItemBinding
import com.noto.app.domain.NotoColor
import com.noto.app.domain.takeLines
import com.noto.app.ui.folder.NoteItemModel
import com.noto.app.ui.util.*

class NoteListWidgetAdapter(
    context: Context,
    layoutResourceId: Int,
    notes: List<NoteItemModel>,
    private val isShowCreationDate: Boolean,
    private val color: NotoColor,
    private val previewSize: Int,
) : ArrayAdapter<NoteItemModel>(context, layoutResourceId, notes) {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = LayoutInflater.from(context)
        return WidgetNoteItemBinding.inflate(layoutInflater, parent, false).withBinding {
            getItem(position)?.let { model ->
                root.context?.let { context ->
                    val colorResource = context.colorResource(color.toColorResourceId())
                    tvNoteTitle.setLinkTextColor(colorResource)
                    tvNoteBody.setLinkTextColor(colorResource)
                    if (isShowCreationDate)
                        tvCreationDate.text = context.stringResource(R.string.created, model.note.creationDate.format(root.context))
                }
                tvNoteTitle.text = model.note.title
                tvCreationDate.isVisible = isShowCreationDate
                tvNoteTitle.isVisible = model.note.title.isNotBlank()
                tvNoteTitle.maxLines = 3
                llLabels.isVisible = model.note.labels.isNotEmpty()
                if (model.note.title.isBlank() && previewSize == 0) {
                    tvNoteBody.text = model.note.body.takeLines(1)
                    tvNoteBody.maxLines = 1
                    tvNoteBody.isVisible = true
                } else {
                    tvNoteBody.text = model.note.body.takeLines(previewSize)
                    tvNoteBody.maxLines = previewSize
                    tvNoteBody.isVisible = previewSize != 0 && model.note.body.isNotBlank()
                }
                tvNoteTitle.setPadding(0.dp, 0.dp, 0.dp, if (model.note.body.isBlank() || previewSize == 0) 0.dp else 4.dp)
                tvNoteBody.setPadding(if (model.note.title.isBlank()) 0.dp else 4.dp, 0.dp, 0.dp, 0.dp)
                model.note.labels.forEach { label ->
                    NoteLabelItemBinding.inflate(layoutInflater, parent, false).withBinding {
                        tvLabel.text = label.title
                        tvLabel.background?.mutate()?.setTint(context.colorResource(color.toColorResourceId()))
                        tvLabel.setTextColor(context.colorAttributeResource(R.attr.notoBackgroundColor))
                        llLabels.addView(root)
                    }
                }

                if (isCurrentLocaleArabic()) {
                    tvCreationDate.typeface = root.context?.tryLoadingFontResource(R.font.nunito_semibold)
                } else {
                    tvCreationDate.typeface = root.context?.tryLoadingFontResource(R.font.nunito_semibold_italic)
                }
            }
        }
    }
}