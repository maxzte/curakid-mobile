package com.misterio.core.domain.model

import java.util.UUID

data class LifePeriodAiReport(
    val id: UUID,
    val childId: UUID,
    val lifePeriodId: UUID?,
    val triggerMeasurementId: UUID?,
    val reportText: String?,
    val recommendations: String?,
    val urgencyScore: Double?,
    val isFallback: Boolean,
)
