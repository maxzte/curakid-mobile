package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.sync.Syncable
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

interface EventRepository : Syncable {

    fun getEvents(childId: UUID): Flow<List<Event>>

    fun getEventById(eventId: UUID): Flow<Event?>

    fun getEventsByDate(childId: UUID, date: LocalDateTime): Flow<List<Event>>

    suspend fun addEvent(childId: UUID, event: Event): Result<Unit>

    suspend fun addEvents(childId: UUID, events: List<Event>): Result<Unit>

    suspend fun updateEvent(childId: UUID, event: Event): Result<Unit>

    suspend fun deleteEvent(eventId: UUID): Result<Unit>

    suspend fun syncEvents(childId: UUID): Result<Unit>

}
