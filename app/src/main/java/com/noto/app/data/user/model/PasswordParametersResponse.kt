package com.noto.app.data.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordParametersResponse(
    @SerialName("password_parameters")
    val passwordParameters: String, // $argon2id$v=19$m=4096,t=3,p=1
)