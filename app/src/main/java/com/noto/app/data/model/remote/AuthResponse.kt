package com.noto.app.data.model.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String, // eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNjY2NjcxNTEwLCJzdWIiOiJmY2QxZGEzZi03MGI3LTRmMDItODFmYS1kMTI5YmMyNWFhNzEiLCJlbWFpbCI6ImFsaUBhbGJhYWxpLmNvbSIsInBob25lIjoiIiwiYXBwX21ldGFkYXRhIjp7InByb3ZpZGVyIjoiZW1haWwiLCJwcm92aWRlcnMiOlsiZW1haWwiXX0sInVzZXJfbWV0YWRhdGEiOnt9LCJyb2xlIjoiYXV0aGVudGljYXRlZCIsInNlc3Npb25faWQiOiIyYmI5NGVjMy0yYWJlLTRjNDEtODBkNy1jNjQ3NTdhZDQ3OWYifQ._KxSmdRE_nNxSsHu66OO8vjZMdfNllfyAS6PlbfwReo
    @SerialName("token_type")
    val tokenType: String, // bearer
    @SerialName("expires_in")
    val expiresIn: Int, // 3600
    @SerialName("refresh_token")
    val refreshToken: String, // UqLdSE6dEjr9bO-eDVrG_g
    @SerialName("user")
    val user: RemoteAuthUser,
)