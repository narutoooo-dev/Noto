package com.noto.app.data.model.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignUpErrorResponse(
    @SerialName("code")
    val code: Int, // 400
    @SerialName("msg")
    val msg: String // Error invalid request body
)