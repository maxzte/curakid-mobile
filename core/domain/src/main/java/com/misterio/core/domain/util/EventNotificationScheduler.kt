package com.misterio.core.domain.util

import com.misterio.core.domain.model.Event
import java.util.UUID

interface EventNotificationScheduler {

    suspend fun schedule(event: Event)

    suspend fun schedule(events: List<Event>)

    suspend fun cancel(eventId: UUID)

    suspend fun cancelAll(childId: UUID)

    suspend fun cancelAllEvents()

}
