package com.noto.app.domain.label

import com.noto.app.domain.NotoColor

data class Label(
    val id: Long,
    val folderId: Long,
    val title: String,
    val color: NotoColor,
    val position: Int,
) {
    companion object {
        val Default = Label(
            id = 0L,
            folderId = -1,
            title = String(),
            color = NotoColor.Gray,
            position = 0,
        )
    }
}