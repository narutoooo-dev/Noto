package com.noto.app.data.user.model

import kotlinx.serialization.Serializable

@Serializable
data class IsEmailExistResponse(
    val isExist: Boolean,
)
