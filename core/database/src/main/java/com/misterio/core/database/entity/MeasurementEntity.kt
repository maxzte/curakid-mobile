package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

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
    @PrimaryKey val id: String,
    val childId: String,
    val weightKg: Double?,
    val heightCm: Double?,
    val headCircCm: Double?,
    val wfaZscore: Double? = null,
    val hfaZscore: Double? = null,
    val bmiZscore: Double? = null,
    val hcfaZscore: Double? = null,
    val measurementDate: LocalDateTime,
    val isSynced: Boolean = true,
    val aiReportId: String?
)
