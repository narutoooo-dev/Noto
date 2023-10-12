package com.noto.app.data.model.remote.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEmailRequest(
    val email: String
)