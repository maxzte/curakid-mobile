package com.misterio.core.domain.util

import com.misterio.core.domain.model.Event

interface EventNotificationScheduler {

    suspend fun schedule(event: Event)

    suspend fun schedule(events: List<Event>)

    suspend fun cancel(eventId: String)

    suspend fun cancelAll(childId: String)

}
