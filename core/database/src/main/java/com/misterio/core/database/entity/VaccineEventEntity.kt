package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "vaccines_event")
data class VaccineEventEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val name: String,
    val date: LocalDateTime,
    val disease: String?,
    val doseNumber: Int?,
    val totalDoses: Int?,
    val type: String,
    val scheduledAgeMonths: Int?,
    val scheduledAgeDays: Int?,
    val notes: String?,
    val status: String,
)
