package com.misterio.core.domain.usecase

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventQuickPreview
import java.time.LocalDate
import javax.inject.Inject

class GetEventQuickPreviewUseCase @Inject constructor() {
    operator fun invoke(date: LocalDate, events: List<Event>): EventQuickPreview? {
        if (events.isEmpty()) return null
        return EventQuickPreview(
            date = date,
            totalEventsCount = events.size,
            firstEventTitle = events.first().title,
        )
    }
}
