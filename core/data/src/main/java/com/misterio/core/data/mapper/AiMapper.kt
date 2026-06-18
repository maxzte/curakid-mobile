package com.misterio.core.data.mapper

import com.misterio.core.data.util.toLocalDateTime
import com.misterio.core.database.entity.ChildReportEntity
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.network.dto.ReportResponse

fun ReportResponse.toEntity() = ChildReportEntity(
    id = id,
    childId = childId,
    lifePeriodId = lifePeriodId,
    triggerMeasurementId = triggerMeasurementId,
    reportText = reportText,
    recommendations = recommendations,
    isFallback = isFallback,
    urgencyScore = urgencyScore,
    createdAt = createdAt.toLocalDateTime(),
)

fun ChildReportEntity.toDomain() = LifePeriodAiReport(
    id = id,
    childId = childId,
    lifePeriodId = lifePeriodId,
    triggerMeasurementId = triggerMeasurementId,
    reportText = reportText,
    recommendations = recommendations,
    isFallback = isFallback,
    urgencyScore = urgencyScore
)
