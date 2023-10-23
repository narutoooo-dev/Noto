package com.noto.app.domain.model

data class Label(
    val id: Long = 0L,
    val folderId: Long,
    val title: String = "",
    val color: NotoColor = NotoColor.Gray,
    val position: Int = 0,
)