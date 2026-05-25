package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.database.dao.EventDao
import com.misterio.core.database.entity.EventEntity
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.VaccineType
import com.misterio.core.domain.repository.EventRepository
import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.core.network.api.EventApi
import com.misterio.core.network.dto.UpdateEventRequest
import com.misterio.core.network.dto.toDomain
import com.misterio.core.network.dto.toRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventApi: EventApi,
    private val notificationScheduler: EventNotificationScheduler
) : EventRepository {

    override fun getEvents(childId: String): Flow<List<Event>> =
        eventDao.getEvents(childId).map { entities -> entities.map { it.toDomain() } }

    override fun getEventById(eventId: String): Flow<Event?> =
        eventDao.getEventById(eventId).map { it?.toDomain() }

    override fun getEventsByDate(childId: String, date: LocalDateTime): Flow<List<Event>> =
        eventDao.getEventsByDate(childId, date)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun addEvent(childId: String, event: Event): Result<Unit> = runCatching {
        // 1. Зберігаємо локально
        eventDao.insertEvent(event.toEntity(childId))


        // 2. Плануємо сповіщення
        notificationScheduler.schedule(listOf(event))

        // 3. Синхронізація (краще винести в WorkManager, але якщо потрібно тут:)
        syncEvents(childId, listOf(event))
    }

    override suspend fun addEvents(childId: String, events: List<Event>): Result<Unit> =
        runCatching {
            eventDao.insertEvents(events.map { it.toEntity(childId) })
            notificationScheduler.schedule(events)

            syncEvents(childId, events)
        }

    override suspend fun updateEvent(childId: String, event: Event): Result<Unit> = runCatching {
        // 1. Update locally
        eventDao.insertEvent(event.toEntity(childId))

        // 2. Reschedule notification
        notificationScheduler.cancel(event.id)
        notificationScheduler.schedule(listOf(event))

        // 3. Sync with server
        syncUpdateEvent(childId, event)
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        // 1. Delete locally first (priority for UX)
        eventDao.deleteEvent(eventId)
        notificationScheduler.cancel(eventId)

        // 2. Try to delete on backend
        try {
            val response = eventApi.deleteEvent(UUID.fromString(eventId))
            if (!response.isSuccessful && response.code() != 404) {
                // If failed (not 404), we could potentially mark it for background sync
                // but for now, we just log the failure.
                Log.e(
                    "EventRepository",
                    "Failed to delete event $eventId on server: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            Log.e("EventRepository", "Error deleting event $eventId on server", e)
        }

        Result.success(Unit)
    }

    override suspend fun syncEvents(childId: String): Result<Unit> = runCatching {
        // 1. Pull events from backend (General and Vaccines)
        val generalResponse = eventApi.getEvents(UUID.fromString(childId))
        val vaccineResponse = eventApi.getVaccineEventsFor(childId)

        val remoteEvents = mutableListOf<Event>()
        if (generalResponse.isSuccessful) {
            remoteEvents.addAll(generalResponse.body()?.map { it.toDomain() } ?: emptyList())
        }
        if (vaccineResponse.isSuccessful) {
            remoteEvents.addAll(vaccineResponse.body()?.map { it.toDomain() } ?: emptyList())
        }

        val entities = remoteEvents.map { it.toEntity(childId) }

        // Get IDs of events to be deleted to cancel notifications
        val remainingIds = entities.map { it.id }
        val deletedIds = eventDao.getDeletedEventIds(childId, remainingIds)

        // Cancel notifications for deleted events
        deletedIds.forEach { eventId ->
            notificationScheduler.cancel(eventId)
        }

        // Sync local DB with server
        eventDao.syncWithServer(childId, entities)

        // Reschedule notifications for all active events from server
        notificationScheduler.schedule(remoteEvents)

        // 2. Send unsynced local events
        val unsynced = eventDao.getUnsyncedEvents(childId)
        if (unsynced.isNotEmpty()) {
            Log.d("EventRepository", "Syncing ${unsynced.size} unsynced events for child $childId")
            syncEvents(childId, unsynced.map { it.toDomain() })
        }
    }

    private suspend fun syncEvents(childId: String, events: List<Event>) {
        try {
            if (events.isEmpty()) return

            val response = eventApi.createEvents(
                UUID.fromString(childId),
                events.map { it.toRequest(childId) })
            if (response.isSuccessful) {
                eventDao.markAsSynced(events.map { it.id })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncUpdateEvent(childId: String, event: Event) {
        try {
            val request = UpdateEventRequest(
                title = event.title,
                type = event.type,
                date = event.date.atZone(ZoneId.systemDefault()).toInstant().toString(),
                location = if (event is Event.General) event.location else null,
                description = event.description,
                status = event.status
            )
            val response =
                eventApi.updateEvent(UUID.fromString(childId), UUID.fromString(event.id), request)
            if (response.isSuccessful) {
                eventDao.markAsSynced(event.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun EventEntity.toDomain(): Event {
    val type = EventType.valueOf(type)
    return if (type == EventType.VACCINE) {
        Event.Vaccine(
            id = id,
            title = title,
            date = date,
            description = description,
            status = EventStatus.valueOf(status),
            isSynced = isSynced,
            disease = disease,
            doseNumber = doseNumber,
            totalDoses = totalDoses,
            vaccineType = vaccineType?.let { VaccineType.valueOf(it) } ?: VaccineType.VACCINATION,
            scheduledAgeMonths = scheduledAgeMonths,
            scheduledAgeDays = scheduledAgeDays,
            notes = notes
        )
    } else {
        Event.General(
            id = id,
            title = title,
            type = type,
            date = date,
            description = description,
            status = EventStatus.valueOf(status),
            isSynced = isSynced,
            location = location
        )
    }
}

fun Event.toEntity(childId: String) = when (this) {
    is Event.General -> EventEntity(
        id = id,
        childId = childId,
        title = title,
        type = type.name,
        date = date,
        description = description,
        status = status.name,
        isSynced = isSynced,
        location = location
    )
    is Event.Vaccine -> EventEntity(
        id = id,
        childId = childId,
        title = title,
        type = type.name,
        date = date,
        description = description,
        status = status.name,
        isSynced = isSynced,
        disease = disease,
        doseNumber = doseNumber,
        totalDoses = totalDoses,
        vaccineType = vaccineType.name,
        scheduledAgeMonths = scheduledAgeMonths,
        scheduledAgeDays = scheduledAgeDays,
        notes = notes
    )
}
