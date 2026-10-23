package com.misterio.core.network.dto

data class RegisterDeviceTokenRequest(
    val token: String,
    val deviceType: String = "ANDROID"
)

data class DeviceTokenResponse(
    val id: String,
    val token: String,
    val deviceType: String,
    val lastActiveAt: String
)
