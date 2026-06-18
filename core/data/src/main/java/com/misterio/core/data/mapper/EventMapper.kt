package com.misterio.core.data.mapper

import com.misterio.core.data.util.toLocalDateTime
import com.misterio.core.database.entity.EventEntity
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.VaccineType
import com.misterio.core.network.dto.EventResponse
import com.misterio.core.network.dto.VaccineEventResponse
import java.util.UUID

fun EventResponse.toDomain() = Event.General(
    id = id,
    title = title,
    type = type,
    date = date.toLocalDateTime(),
    location = location,
    description = description,
    status = status,
    isSynced = true
)

fun VaccineEventResponse.toDomain() = Event.Vaccine(
    id = id,
    title = name,
    date = date.toLocalDateTime(),
    status = status,
    description = description,
    isSynced = true,
    disease = disease,
    doseNumber = doseNumber,
    totalDoses = totalDoses,
    vaccineType = vaccineType,
    scheduledAgeMonths = scheduledAgeMonths,
    scheduledAgeDays = scheduledAgeDays,
    notes = notes
)

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
            notes = notes,
            isDeleted = isDeleted
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
            location = location,
            isDeleted = isDeleted
        )
    }
}

fun Event.toEntity(childId: UUID, isDeleted: Boolean = false) = when (this) {
    is Event.General -> EventEntity(
        id = id,
        childId = childId,
        title = title,
        type = type.name,
        date = date,
        description = description,
        status = status.name,
        isSynced = isSynced,
        location = location,
        isDeleted = isDeleted
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
        notes = notes,
        isDeleted = isDeleted
    )
}
