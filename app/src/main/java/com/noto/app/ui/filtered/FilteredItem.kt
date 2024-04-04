package com.noto.app.ui.filtered

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.noto.app.R
import com.noto.app.databinding.FilteredItemBinding
import com.noto.app.domain.FilteredItem
import com.noto.app.ui.util.*

private val StrokeWidth = 1.dp

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass
abstract class FilteredItem : EpoxyModelWithHolder<com.noto.app.ui.filtered.FilteredItem.Holder>() {

    @EpoxyAttribute
    lateinit var model: FilteredItem

    @EpoxyAttribute
    open var isShowNotesCount: Boolean = true

    @EpoxyAttribute
    var notesCount: Int = 0

    @EpoxyAttribute
    open var isSelected: Boolean = false

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var onClickListener: View.OnClickListener

    @SuppressLint("ClickableViewAccessibility")
    override fun bind(holder: Holder) = with(holder.binding) {
        root.isSelected = isSelected
        root.setOnClickListener(onClickListener)
        tvNotesCount.isVisible = isShowNotesCount
        tvNotesCount.text = notesCount.toString()
        root.strokeWidth = StrokeWidth
        root.context?.let { context ->
            when (model) {
                FilteredItem.All -> {
                    tvTitle.text = context.stringResource(R.string.all)
                    ivIcon.setImageResource(R.drawable.ic_round_all_notes_24)
                }

                FilteredItem.Recent -> {
                    tvTitle.text = context.stringResource(R.string.recent)
                    ivIcon.setImageResource(R.drawable.ic_round_schedule_24)
                }

                FilteredItem.Scheduled -> {
                    tvTitle.text = context.stringResource(R.string.scheduled)
                    ivIcon.setImageResource(R.drawable.ic_round_notifications_active_24)
                }

                FilteredItem.Archived -> {
                    tvTitle.text = context.stringResource(R.string.archived)
                    ivIcon.setImageResource(R.drawable.ic_round_inventory_24)
                }
            }
            val colorResource = context.colorResource(model.color.toColorResourceId())
            val backgroundColorResource = context.colorAttributeResource(R.attr.notoBackgroundColor)
            root.rippleColor = colorResource.toColorStateList()
            root.strokeColor = colorResource
            if (isSelected) {
                root.setCardBackgroundColor(colorResource.withDefaultAlpha())
                tvTitle.setTextColor(colorResource)
                ivIcon.imageTintList = colorResource.toColorStateList()
                tvNotesCount.setTextColor(colorResource)
            } else {
                root.setCardBackgroundColor(backgroundColorResource)
                tvTitle.setTextColor(colorResource)
                ivIcon.imageTintList = colorResource.toColorStateList()
                tvNotesCount.setTextColor(colorResource)
            }
        } ?: Unit
    }

    override fun getDefaultLayout(): Int = R.layout.filtered_item

    class Holder : EpoxyHolder() {
        lateinit var binding: FilteredItemBinding
            private set

        override fun bindView(itemView: View) {
            binding = FilteredItemBinding.bind(itemView)
        }
    }
}