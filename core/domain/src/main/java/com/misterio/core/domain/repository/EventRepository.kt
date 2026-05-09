package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface EventRepository {
    fun getEvents(childId: String): Flow<List<Event>>
    fun getEventsByDate(childId: String, date: LocalDateTime): Flow<List<Event>>
    suspend fun addEvent(childId: String, event: Event): Result<Unit>
    suspend fun deleteEvent(eventId: String): Result<Unit>
    suspend fun syncEvents(childId: String): Result<Unit>
}
