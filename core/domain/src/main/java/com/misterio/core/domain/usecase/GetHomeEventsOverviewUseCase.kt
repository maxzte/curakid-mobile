package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.HomeEventsOverview
import java.time.LocalDate
import javax.inject.Inject

class GetHomeEventsOverviewUseCase @Inject constructor() {

    operator fun invoke(
        events: List<Event>,
        currentDate: LocalDate = LocalDate.now(),
    ): HomeEventsOverview {
        val activeEvents = events.sortedBy { it.date }

        // Select items for Hero Cards
        val v = activeEvents.filterIsInstance<Event.Vaccine>()
            .firstOrNull { it.status == EventStatus.PLANNED }

        val e = activeEvents.firstOrNull { it.type == EventType.DOCTOR && it.status == EventStatus.PLANNED }
            ?: activeEvents.firstOrNull { it !is Event.Vaccine && it.status == EventStatus.PLANNED }

        val allUpcoming = activeEvents
            .filter { it.date.toLocalDate() >= currentDate }

        // Skip items shown in Hero Cards
        val upcomingItems = allUpcoming.filter { it.id != v?.id && it.id != e?.id }

        return HomeEventsOverview(
            vaccineEvent = v,
            doctorEvent = e,
            upcomingItems = upcomingItems,
        )
    }
}
