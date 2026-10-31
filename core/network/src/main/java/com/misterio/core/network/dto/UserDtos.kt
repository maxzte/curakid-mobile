package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class UserResponse(
    @SerializedName("id") val id: UUID,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String?,
    @SerializedName("onboardingCompleted") val onboardingCompleted: Boolean,
    @SerializedName("active") val active: Boolean
)

data class UpdateUserRequest(
    @SerializedName("name") val name: String
)
