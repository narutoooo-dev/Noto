package com.noto.app.data.model.remote.response

import kotlinx.serialization.Serializable

@Serializable
data class IsEmailExistResponse(
    val isExist: Boolean
)
