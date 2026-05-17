package com.misterio.curakid.feature.calendar

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import java.time.LocalDate
import java.time.LocalDateTime

data class CalendarUiState(
    val selectedMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val selectedDate: LocalDate = LocalDate.now(),
    val filteredEvents: List<Event> = emptyList(),
    val allEvents: List<Event> = emptyList(),
    val childName: String = "",
    val vaccinesQuantity: Int = 0,
    val vaccinesDone: Int = 0,
    val isEventSheetOpen: Boolean = false,
    val eventForm: EventFormState = EventFormState(),
    val isEventsExpanded: Boolean = false,
    val quickPreview: String? = null
)

data class EventFormState(
    val id: String? = null,
    val title: String = "",
    val type: EventType = EventType.USER,
    val date: LocalDateTime = LocalDateTime.now(),
    val location: String? = null,
    val description: String? = null,
    val status: EventStatus = EventStatus.PLANNED
)
