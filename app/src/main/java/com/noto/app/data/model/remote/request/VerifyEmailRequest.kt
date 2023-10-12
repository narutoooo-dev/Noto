package com.noto.app.data.model.remote.request

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailRequest(
    val type: String,
    val email: String,
)