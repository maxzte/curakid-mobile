package com.misterio.feature.calendar

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import java.time.LocalDate
import java.time.LocalDateTime

data class CalendarUiState(
    val selectedMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<Event> = emptyList(),
    val vaccinesQuantity: Int = 0,
    val vaccinesDone: Int = 0,
    val isEventSheetOpen: Boolean = false,
    val eventForm: EventFormState = EventFormState()
)

data class EventFormState(
    val title: String = "",
    val type: EventType = EventType.USER,
    val date: LocalDateTime = LocalDateTime.now(),
    val location: String? = null,
    val description: String? = null
)
