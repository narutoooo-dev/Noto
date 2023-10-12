package com.noto.app.data.model.remote


import kotlinx.serialization.Serializable

@Serializable
data class UserMetadata(
    val name: String,
    val passwordParameters: String,
)