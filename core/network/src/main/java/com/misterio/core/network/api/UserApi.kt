package com.misterio.core.network.api

import com.misterio.core.network.dto.UpdateUserRequest
import com.misterio.core.network.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("api/v1/users/me")
    suspend fun updateCurrentUser(
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @DELETE("api/v1/users/me")
    suspend fun deleteCurrentUser(): Response<Unit>

}
