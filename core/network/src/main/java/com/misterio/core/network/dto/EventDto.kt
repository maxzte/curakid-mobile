package com.misterio.core.network.dto

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import java.time.LocalDateTime
import java.util.UUID

data class EventRequest(
    val title: String,
    val type: EventType,
    val date: LocalDateTime,
    val location: String? = null,
    val description: String? = null,
    val childId: String,
)

data class UpdateEventRequest(
    val title: String? = null,
    val type: EventType? = null,
    val date: LocalDateTime? = null,
    val location: String? = null,
    val description: String? = null
)

data class EventResponse(
    val id: UUID,
    val title: String,
    val type: EventType,
    val date: LocalDateTime,
    val location: String?,
    val description: String?,
    val childId: UUID
)

fun EventResponse.toExternalModel() = Event(
    id = id.toString(),
    title = title,
    type = type,
    date = date,
    location = location,
    description = description,
    isSynced = true
)

fun Event.toRequest(childId: String) = EventRequest(
    title = title,
    type = type,
    date = date,
    location = location,
    description = description,
    childId = childId
)
