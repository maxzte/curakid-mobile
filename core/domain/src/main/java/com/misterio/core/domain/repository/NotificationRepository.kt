package com.misterio.core.domain.repository

interface NotificationRepository {
    suspend fun registerDeviceToken(token: String): Result<Unit>
    suspend fun removeDeviceToken(token: String): Result<Unit>
}
