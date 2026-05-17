package com.misterio.core.domain.model

data class LifePeriodAiReport(
    val id: String,
    val childId: String,
    val lifePeriodId: String?,
    val triggerMeasurementId: String?,
    val reportText: String?,
    val recommendations: String?,
    val urgencyScore: Double?,
    val isFallback: Boolean,
)
