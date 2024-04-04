package com.noto.app.ui.label

import com.noto.app.domain.label.Label

data class LabelItemModel(
    val label: Label,
    val isSelected: Boolean,
)