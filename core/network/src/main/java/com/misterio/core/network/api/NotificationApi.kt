package com.misterio.core.network.api

import com.misterio.core.network.dto.DeviceTokenResponse
import com.misterio.core.network.dto.RegisterDeviceTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID

interface NotificationApi {

    @POST("api/v1/notifications/device-token")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): Response<DeviceTokenResponse>

    @DELETE("api/v1/notifications/device-token")
    suspend fun removeDeviceToken(
        @Query("token") token: String
    ): Response<Unit>

    @POST("api/v1/notifications/trigger-reminders")
    suspend fun triggerReminders(): Response<Map<String, String>>

    @POST("api/v1/notifications/test-push")
    suspend fun sendTestPush(
        @Query("userId") userId: UUID,
        @Query("title") title: String? = null,
        @Query("body") body: String? = null
    ): Response<Map<String, String>>
}
