package com.misterio.core.network.dto

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.VaccineType
import java.time.LocalDateTime
import java.util.UUID

data class EventRequest(
    val title: String,
    val type: EventType,
    val date: String,
    val location: String? = null,
    val description: String? = null,
    val childId: String,
)

data class UpdateEventRequest(
    val title: String? = null,
    val type: EventType? = null,
    val date: String? = null,
    val location: String? = null,
    val description: String? = null,
    val status: EventStatus? = null,
)

data class EventResponse(
    val childId: UUID,
    val id: UUID,
    val type: EventType,
    val date: String,
    val status: EventStatus,

    val title: String,
    val location: String?,
    val description: String?
)

data class VaccineEventResponse(
    val childId: String,
    val id: String,
    val type: EventType,
    val date: String,
    val status: EventStatus,

    val name: String,
    val disease: String?,
    val doseNumber: Int?,
    val totalDoses: Int?,
    val scheduledAgeMonths: Int?,
    val scheduledAgeDays: Int?,
    val administeredDate: String?,
    val vaccineType: VaccineType,
    val notes: String?,
)

fun EventResponse.toDomain() = Event(
    status = status,
    id = id.toString(),
    title = title,
    type = type,
    date = LocalDateTime.parse(date),
    location = location,
    description = description,
    isSynced = true
)

fun Event.toRequest(childId: String) = EventRequest(
    title = title,
    type = type,
    date = date.toString(),
    location = location,
    description = description,
    childId = childId
)
