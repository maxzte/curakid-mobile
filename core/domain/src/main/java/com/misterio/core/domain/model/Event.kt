package com.misterio.core.domain.model

import java.time.LocalDateTime

enum class EventType {
    VACCINE,
    DOCTOR,
    USER
}

data class Event(
    val id: String,
    val title: String,
    val type: EventType,
    val date: LocalDateTime,
    val location: String? = null,
    val description: String? = null,
    val isSynced: Boolean = false
)
