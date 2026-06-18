package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "measurements",
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
data class MeasurementEntity(
    @PrimaryKey val id: UUID,
    val childId: UUID,
    val measurementDate: LocalDateTime,
    val aiReportId: UUID?,

    val weightKg: Double?,
    val heightCm: Double?,
    val headCircCm: Double?,

    val wfaZscore: Double? = null,
    val hfaZscore: Double? = null,
    val wfhZscore: Double? = null,
    val bmiZscore: Double? = null,
    val hcfaZscore: Double? = null,

    val wfaPercentile: Double? = null,
    val hfaPercentile: Double? = null,
    val wfhPercentile: Double? = null,
    val bmiPercentile: Double? = null,
    val hcfaPercentile: Double? = null,

    val isSynced: Boolean = true,
)
