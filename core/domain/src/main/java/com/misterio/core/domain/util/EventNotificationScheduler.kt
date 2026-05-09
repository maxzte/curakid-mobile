package com.misterio.core.domain.util

import com.misterio.core.domain.model.Event

interface EventNotificationScheduler {
    fun schedule(event: Event)
}
