package com.noto.app.data.model.remote.request

import com.noto.app.data.model.remote.UserMetadata
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: UserMetadata,
)