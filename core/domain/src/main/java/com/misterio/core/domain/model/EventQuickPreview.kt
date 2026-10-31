package com.misterio.core.domain.model

import java.time.LocalDate

data class EventQuickPreview(
    val date: LocalDate,
    val totalEventsCount: Int,
    val firstEventTitle: String,
)
