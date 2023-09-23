package com.noto.app.data.model.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteAuthUser(
    @SerialName("id")
    val id: String, // db1322ea-8613-44ae-8d89-c9f0cff631a0
    @SerialName("aud")
    val aud: String, // authenticated
    @SerialName("role")
    val role: String, // authenticated
    @SerialName("email")
    val email: String, // someone1@email.com
    @SerialName("phone")
    val phone: String,
    @SerialName("confirmation_sent_at")
    val confirmationSentAt: String, // 2022-10-25T02:27:38.795715714Z
    @SerialName("app_metadata")
    val appMetadata: AppMetadata,
    @SerialName("user_metadata")
    val userMetadata: UserMetadata,
    @SerialName("identities")
    val identities: List<Identity>,
    @SerialName("created_at")
    val createdAt: String, // 2022-10-25T02:27:38.768582Z
    @SerialName("updated_at")
    val updatedAt: String, // 2022-10-25T02:27:40.459654Z
)