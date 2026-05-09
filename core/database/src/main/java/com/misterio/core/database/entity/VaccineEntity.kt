package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "vaccines")
data class VaccineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val disease: String,
    val doseNumber: Int,
    val totalDoses: Int,
    val type: String,
    val scheduledAgeMonths: Int?,
    val scheduledAgeDays: Int?,
    val administeredDate: LocalDate?,
    val batchNumber: String?,
    val administeredBy: String?,
    val notes: String?,
    val status: String,
)
