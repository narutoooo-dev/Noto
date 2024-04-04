package com.noto.app.ui.component.util

import android.annotation.SuppressLint
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.noto.app.R
import com.noto.app.databinding.PlaceholderItemBinding
import com.noto.app.ui.theme.NotoTheme
import com.noto.app.ui.util.setFullSpan

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass
abstract class PlaceholderItem : EpoxyModelWithHolder<PlaceholderItem.Holder>() {

    @EpoxyAttribute
    lateinit var placeholder: String

    override fun bind(holder: Holder) = with(holder.binding) {
        tvPlaceholder.text = placeholder
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        super.onViewAttachedToWindow(holder)
        holder.binding.root.setFullSpan()
    }

    override fun getDefaultLayout(): Int = R.layout.placeholder_item

    class Holder : EpoxyHolder() {
        lateinit var binding: PlaceholderItemBinding
            private set

        override fun bindView(itemView: View) {
            binding = PlaceholderItemBinding.bind(itemView)
        }
    }
}

@Composable
fun PlaceholderItem(placeholder: String, modifier: Modifier = Modifier) {
    Text(
        text = placeholder,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(NotoTheme.dimensions.medium),
    )
}