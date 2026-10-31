package com.misterio.core.data.repository

import com.misterio.core.data.util.safeApiCall
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.repository.EventRepository
import com.misterio.core.domain.repository.NotificationRepository
import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.core.domain.util.FcmRegistrar
import com.misterio.core.network.api.NotificationApi
import com.misterio.core.network.dto.RegisterDeviceTokenRequest
import com.misterio.curakid.core.common.AppDataStore
import com.misterio.curakid.core.common.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
    private val appDataStore: AppDataStore,
    private val settingsManager: SettingsManager,
    private val childRepository: ChildRepository,
    private val eventRepository: EventRepository,
    private val eventNotificationScheduler: EventNotificationScheduler,
    private val fcmRegistrar: FcmRegistrar,
) : NotificationRepository {

    override suspend fun registerDeviceToken(token: String): Result<Unit> {
        return safeApiCall {
            notificationApi.registerDeviceToken(
                RegisterDeviceTokenRequest(token = token, deviceType = "ANDROID"),
            )
        }.map {}
    }

    override suspend fun removeDeviceToken(token: String): Result<Unit> {
        return safeApiCall {
            notificationApi.removeDeviceToken(token)
        }.map {}
    }

    override fun isPermissionRequested(): Flow<Boolean> {
        return appDataStore.notificationPermissionRequested
    }

    override suspend fun setPermissionRequested(requested: Boolean) {
        appDataStore.setNotificationPermissionRequested(requested)
    }

    override fun isNotificationsEnabled(): Flow<Boolean> {
        return settingsManager.isNotificationsEnabled
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
        settingsManager.saveNotificationsEnabled(enabled)
        if (enabled) {
            val child = childRepository.getSelectedChild().firstOrNull()
            if (child != null) {
                val events = eventRepository.getEvents(child.id).firstOrNull() ?: emptyList()
                eventNotificationScheduler.schedule(events)
            }
        } else {
            eventNotificationScheduler.cancelAllEvents()
        }
        return Result.success(Unit)
    }

    override suspend fun registerFcmToken(): Result<Unit> {
        return fcmRegistrar.register()
    }
}
