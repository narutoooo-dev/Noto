package com.noto.app.data.model.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Identity(
    @SerialName("id")
    val id: String, // db1322ea-8613-44ae-8d89-c9f0cff631a0
    @SerialName("user_id")
    val userId: String, // db1322ea-8613-44ae-8d89-c9f0cff631a0
    @SerialName("identity_data")
    val identityData: IdentityData,
    @SerialName("provider")
    val provider: String, // email
    @SerialName("last_sign_in_at")
    val lastSignInAt: String, // 2022-10-25T02:27:38.785869384Z
    @SerialName("created_at")
    val createdAt: String, // 2022-10-25T02:27:38.785911Z
    @SerialName("updated_at")
    val updatedAt: String, // 2022-10-25T02:27:38.785915Z
)