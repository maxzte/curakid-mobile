package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.data.util.SyncManager
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.EventDao
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.repository.EventRepository
import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.core.network.api.EventApi
import com.misterio.core.network.dto.toRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventApi: EventApi,
    private val notificationScheduler: EventNotificationScheduler,
    private val syncManager: SyncManager,
) : EventRepository {

    override fun getEvents(childId: UUID): Flow<List<Event>> =
        eventDao.getEvents(childId).map { entities -> entities.map { it.toDomain() } }

    override fun getEventById(eventId: UUID): Flow<Event?> =
        eventDao.getEventById(eventId).map { it?.toDomain() }

    override fun getEventsByDate(childId: UUID, date: LocalDateTime): Flow<List<Event>> =
        eventDao.getEventsByDate(childId, date)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun addEvent(childId: UUID, event: Event): Result<Unit> {
        // 1. Save locally
        eventDao.insertEvent(event.toEntity(childId).copy(isSynced = false))
        syncManager.scheduleSync()

        // 2. Schedule notification
        notificationScheduler.schedule(listOf(event))

        return Result.success(Unit)
    }

    override suspend fun addEvents(childId: UUID, events: List<Event>): Result<Unit> {
        eventDao.insertEvents(events.map { it.toEntity(childId).copy(isSynced = false) })
        syncManager.scheduleSync()

        notificationScheduler.schedule(events)

        return Result.success(Unit)
    }

    override suspend fun updateEvent(childId: UUID, event: Event): Result<Unit> {
        // 1. Update locally
        eventDao.insertEvent(event.toEntity(childId).copy(isSynced = false))
        syncManager.scheduleSync()

        // 2. Reschedule notification
        notificationScheduler.cancel(event.id)
        notificationScheduler.schedule(listOf(event))

        return Result.success(Unit)
    }

    override suspend fun deleteEvent(eventId: UUID): Result<Unit> {
        val event = eventDao.getEventById(eventId).map { it?.toDomain() }.first()

        if (event == null) {
            return Result.success(Unit)
        }

        notificationScheduler.cancel(eventId)

        if (!event.isSynced) {
            // If never synced, just hard delete locally
            eventDao.hardDeleteEvent(eventId)
            return Result.success(Unit)
        } else {
            // Mark for deletion and schedule sync
            eventDao.softDeleteEvent(eventId)
            syncManager.scheduleSync()
        }

        // 2. Try to delete on backend
        return safeApiCall { eventApi.deleteEvent(eventId) }
            .onSuccess {
                eventDao.hardDeleteEvent(eventId)
            }
            .recover { error ->
                Log.e(TAG, "Error deleting event $eventId on server", error)
                // We still return success because it's marked for background deletion
                Unit
            }.map { Unit }
    }

    override suspend fun syncEvents(childId: UUID): Result<Unit> {
        // 1. Pull events from backend concurrently
        val (generalResult, vaccineResult) = coroutineScope {
            val generalDeferred = async { safeApiCall { eventApi.getEvents(childId) } }
            val vaccineDeferred = async { safeApiCall { eventApi.getVaccineEventsFor(childId) } }
            generalDeferred.await() to vaccineDeferred.await()
        }

        if (generalResult.isFailure || vaccineResult.isFailure) {
            return Result.failure(
                generalResult.exceptionOrNull()
                    ?: vaccineResult.exceptionOrNull()
                    ?: com.misterio.core.domain.model.DomainError.UnknownError("Failed to sync events")
            )
        }

        val remoteEvents = mutableListOf<Event>()
        generalResult.onSuccess { body -> remoteEvents.addAll(body.map { it.toDomain() }) }
        vaccineResult.onSuccess { body -> remoteEvents.addAll(body.map { it.toDomain() }) }

        val entities = remoteEvents.map { it.toEntity(childId) }

        // Get IDs of events to be deleted to cancel notifications
        val remainingIds = entities.map { it.id }
        val deletedIds = if (remainingIds.isNotEmpty()) {
            eventDao.getDeletedEventIds(childId, remainingIds)
        } else {
            eventDao.getAllSyncedEventIds(childId)
        }

        // Cancel notifications for deleted events
        deletedIds.forEach { eventId ->
            notificationScheduler.cancel(eventId)
        }

        // Sync local DB with server
        eventDao.syncWithServer(childId, entities)

        // Reschedule notifications for all active events from server
        notificationScheduler.schedule(remoteEvents)

        // 2. Trigger background sync for unsynced local events
        val unsynced = eventDao.getUnsyncedEvents(childId)
        if (unsynced.isNotEmpty()) {
            syncManager.scheduleSync()
        }

        return Result.success(Unit)
    }

    private companion object {
        private const val TAG = "EventRepository"
    }
}
