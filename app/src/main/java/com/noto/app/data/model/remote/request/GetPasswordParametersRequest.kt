package com.noto.app.data.model.remote.request

import kotlinx.serialization.Serializable

@Serializable
data class GetPasswordParametersRequest(
    val email: String
)