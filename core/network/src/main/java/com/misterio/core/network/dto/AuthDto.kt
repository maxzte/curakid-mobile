package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("onboardingCompleted") val onboardingCompleted: Boolean,
)

data class LegalDocumentResponse(
    val type: String,
    val version: String,
    val url: String,
    val publishedAt: String,
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("consent") val consent: ConsentRequest,
)

data class ConsentRequest(
    val termsAccepted: Boolean,        // true
    val privacyAccepted: Boolean,      // true
    val consentTimestamp: String,      // ISO 8601
    val consentVersion: String,        // "1.0.2"
    val appVersion: String,            // "1.2.0"
    val deviceLocale: String           // "uk-UA"
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class OAuthRequest(
    @SerializedName("idToken") val idToken: String
)

data class VerifyEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
    @SerializedName("newPassword") val newPassword: String
)

data class MessageResponse(
    @SerializedName("message") val message: String
)
