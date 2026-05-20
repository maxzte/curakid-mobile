package com.misterio.core.network.api

import com.misterio.core.network.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshRequest): Response<Unit>

}
