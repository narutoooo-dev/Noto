package com.noto.app.data.model.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdentityData(
    @SerialName("sub")
    val sub: String, // db1322ea-8613-44ae-8d89-c9f0cff631a0
)