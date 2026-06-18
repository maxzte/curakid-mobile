package com.misterio.core.domain.model

import java.time.LocalDateTime
import java.util.UUID

enum class EventStatus {
    PLANNED,
    DONE,
    SKIPPED,
}

enum class EventType {
    VACCINE,
    DOCTOR,
    USER
}

sealed class Event {
    abstract val id: UUID
    abstract val title: String
    abstract val type: EventType
    abstract val date: LocalDateTime
    abstract val description: String?
    abstract val status: EventStatus
    abstract val isSynced: Boolean
    abstract val isDeleted: Boolean

    data class General(
        override val id: UUID,
        override val title: String,
        override val type: EventType,
        override val date: LocalDateTime,
        override val description: String? = null,
        override val status: EventStatus,
        override val isSynced: Boolean = false,
        override val isDeleted: Boolean = false,
        val location: String? = null,
    ) : Event()

    data class Vaccine(
        override val id: UUID,
        override val title: String,
        override val date: LocalDateTime,
        override val description: String? = null,
        override val status: EventStatus,
        override val isSynced: Boolean = false,
        override val isDeleted: Boolean = false,
        val disease: String? = null,
        val doseNumber: Int? = null,
        val totalDoses: Int? = null,
        val vaccineType: VaccineType = VaccineType.VACCINATION,
        val scheduledAgeMonths: Int? = null,
        val scheduledAgeDays: Int? = null,
        val notes: String? = null,
    ) : Event() {
        override val type: EventType = EventType.VACCINE
    }
}

enum class VaccineType {
    VACCINATION,
    REVACCINATION
}
