package com.misterio.core.domain.model

data class HomeEventsOverview(
    val vaccineEvent: Event.Vaccine? = null,
    val doctorEvent: Event? = null,
    val upcomingItems: List<Event> = emptyList(),
)
