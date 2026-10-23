package com.misterio.core.data.repository

import com.misterio.core.data.util.safeApiCall
import com.misterio.core.domain.repository.NotificationRepository
import com.misterio.core.network.api.NotificationApi
import com.misterio.core.network.dto.RegisterDeviceTokenRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override suspend fun registerDeviceToken(token: String): Result<Unit> {
        return safeApiCall {
            notificationApi.registerDeviceToken(
                RegisterDeviceTokenRequest(token = token, deviceType = "ANDROID")
            )
        }.map { Unit }
    }

    override suspend fun removeDeviceToken(token: String): Result<Unit> {
        return safeApiCall {
            notificationApi.removeDeviceToken(token)
        }.map { Unit }
    }
}
