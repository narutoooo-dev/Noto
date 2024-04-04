package com.noto.app.ui.component.material

import android.annotation.SuppressLint
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.noto.app.R
import com.noto.app.databinding.ProgressIndicatorItemBinding
import com.noto.app.domain.NotoColor
import com.noto.app.ui.util.colorResource
import com.noto.app.ui.util.setFullSpan
import com.noto.app.ui.util.toColorResourceId

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass
abstract class ProgressIndicatorItem : EpoxyModelWithHolder<ProgressIndicatorItem.Holder>() {

    @EpoxyAttribute
    var color: NotoColor? = null

    override fun bind(holder: Holder) {
        with(holder.binding) {
            color?.toColorResourceId()?.let { resource ->
                indicator.setIndicatorColor(root.context.colorResource(resource))
            }
        }
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        super.onViewAttachedToWindow(holder)
        holder.binding.root.setFullSpan()
    }

    override fun getDefaultLayout(): Int = R.layout.progress_indicator_item

    class Holder : EpoxyHolder() {
        lateinit var binding: ProgressIndicatorItemBinding
            private set

        override fun bindView(itemView: View) {
            binding = ProgressIndicatorItemBinding.bind(itemView)
        }
    }
}