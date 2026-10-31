package com.misterio.core.network.dto

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.VaccineType
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

data class EventRequest(
    val id: UUID,
    val childId: UUID,
    val title: String,
    val type: EventType,
    val date: String,
    val location: String? = null,
    val description: String? = null,
    val status: EventStatus,
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
    val id: UUID,
    val childId: UUID,
    val type: EventType,
    val date: String,
    val status: EventStatus,
    val title: String,
    val location: String?,
    val description: String?
)

data class VaccineEventResponse(
    val childId: UUID,
    val id: UUID,
    val type: EventType,
    val date: String,
    val status: EventStatus,
    val name: String,
    val disease: String?,
    val doseNumber: Int?,
    val totalDoses: Int?,
    val scheduledAgeMonths: Int?,
    val scheduledAgeDays: Int?,
    val vaccineType: VaccineType,
    val notes: String?,
    val description: String?,
)

fun Event.toRequest(childId: UUID) = EventRequest(
    id = id,
    childId = childId,
    title = title,
    type = type,
    date = date.atZone(ZoneId.systemDefault()).toInstant().toString(),
    location = if (this is Event.General) location else null,
    description = description,
    status = status,
)

fun Event.toUpdateRequest() = UpdateEventRequest(
    title = title,
    type = type,
    date = date.atZone(ZoneId.systemDefault()).toInstant().toString(),
    location = if (this is Event.General) location else null,
    description = description,
    status = status
)
