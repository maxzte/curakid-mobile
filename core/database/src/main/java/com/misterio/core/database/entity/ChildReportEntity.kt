package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "child_reports",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"])]
)
data class ChildReportEntity(
    @PrimaryKey val id: UUID,
    val childId: UUID,
    val lifePeriodId: UUID?,
    val triggerMeasurementId: UUID?,
    val reportText: String?,
    val recommendations: String?,
    val isFallback: Boolean,
    val urgencyScore: Double?,
    val createdAt: LocalDateTime
)
