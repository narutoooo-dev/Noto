package com.noto.app.domain.model

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