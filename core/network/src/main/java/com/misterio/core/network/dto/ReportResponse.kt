package com.misterio.core.network.dto

import java.util.UUID

data class ReportResponse(
    val id: UUID,
    val childId: UUID,
    val lifePeriodId: UUID?,
    val triggerMeasurementId: UUID?,
    val reportText: String?,
    val recommendations: String?,
    val isFallback: Boolean,
    val urgencyScore: Double?,
    val createdAt: String,
)
