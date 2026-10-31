package com.misterio.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun registerDeviceToken(token: String): Result<Unit>
    suspend fun removeDeviceToken(token: String): Result<Unit>
    fun isPermissionRequested(): Flow<Boolean>
    suspend fun setPermissionRequested(requested: Boolean)
    fun isNotificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>
    suspend fun registerFcmToken(): Result<Unit>
}
