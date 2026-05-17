package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "child_reports")
data class ChildReportEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val lifePeriodId: String?,
    val triggerMeasurementId: String?,
    val reportText: String?,
    val recommendations: String?,
    val isFallback: Boolean,
    val urgencyScore: Double?,
    val createdAt: LocalDateTime
)
