package com.misterio.core.network.api

import com.misterio.core.network.dto.AuthResponse
import com.misterio.core.network.dto.ForgotPasswordRequest
import com.misterio.core.network.dto.LoginRequest
import com.misterio.core.network.dto.MessageResponse
import com.misterio.core.network.dto.OAuthRequest
import com.misterio.core.network.dto.RefreshTokenRequest
import com.misterio.core.network.dto.RegisterRequest
import com.misterio.core.network.dto.ResetPasswordRequest
import com.misterio.core.network.dto.VerifyEmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("api/v1/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<AuthResponse>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Unit>

    @POST("api/v1/auth/oauth/google")
    suspend fun oAuthLogin(@Body request: OAuthRequest): Response<AuthResponse>

}
