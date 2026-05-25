package com.misterio.core.network.dto

data class ReportResponse(
    val id: String,
    val childId: String,
    val lifePeriodId: String?,
    val triggerMeasurementId: String?,
    val reportText: String?,
    val recommendations: String?,
    val isFallback: Boolean,
    val urgencyScore: Double?,
    val createdAt: String,
)
