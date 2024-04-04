package com.noto.app.data.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteAuthUser(
    @SerialName("id")
    val id: String, // db1322ea-8613-44ae-8d89-c9f0cff631a0
    @SerialName("email")
    val email: String, // someone1@email.com
    @SerialName("confirmation_sent_at")
    val confirmationSentAt: String, // 2022-10-25T02:27:38.795715714Z
    @SerialName("created_at")
    val createdAt: String, // 2022-10-25T02:27:38.768582Z
    @SerialName("updated_at")
    val updatedAt: String, // 2022-10-25T02:27:40.459654Z
)