package com.misterio.core.data.repository

import com.misterio.core.database.dao.EventDao
import com.misterio.core.database.entity.EventEntity
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.repository.EventRepository
import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.core.network.api.EventApi
import com.misterio.core.network.dto.toExternalModel
import com.misterio.core.network.dto.toRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventApi: EventApi,
    private val notificationScheduler: EventNotificationScheduler
) : EventRepository {

    override fun getEvents(childId: String): Flow<List<Event>> =
        eventDao.getEvents(childId).map { entities ->
            entities.map { it.toExternalModel() }
        }

    override fun getEventsByDate(childId: String, date: LocalDateTime): Flow<List<Event>> =
        eventDao.getEventsByDate(childId, date).map { entities ->
            entities.map { it.toExternalModel() }
        }

    override suspend fun addEvent(childId: String, event: Event): Result<Unit> = runCatching {
        // 1. Save locally
        eventDao.insertEvent(event.toEntity(childId))

        // 2. Schedule notification
        notificationScheduler.schedule(event)

        // 3. Try to sync immediately
        val response = eventApi.createEvent(event.toRequest(childId))
        if (response.isSuccessful) {
            eventDao.markAsSynced(event.id)
        }
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        // 1. Delete locally
        eventDao.deleteEvent(eventId)

        // 2. Try to delete remotely
        eventApi.deleteEvent(UUID.fromString(eventId))
    }

    override suspend fun syncEvents(childId: String): Result<Unit> = runCatching {
        val unsynced = eventDao.getUnsyncedEvents()
        unsynced.forEach { entity ->
            val response = eventApi.createEvent(entity.toExternalModel().toRequest(childId))
            if (response.isSuccessful) {
                eventDao.markAsSynced(entity.id)
            }
        }
    }
}

fun EventEntity.toExternalModel() = Event(
    id = id,
    title = title,
    type = EventType.valueOf(type),
    date = date,
    location = location,
    description = description,
    isSynced = isSynced
)

fun Event.toEntity(childId: String) = EventEntity(
    id = id,
    childId = childId,
    title = title,
    type = type.name,
    date = date,
    location = location,
    description = description,
    isSynced = isSynced
)
