package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: UUID,
    val childId: UUID,
    val title: String,
    val type: String,
    val date: LocalDateTime,
    val description: String?,
    val status: String,
    val isSynced: Boolean,
    val isDeleted: Boolean = false,
    
    // General event fields
    val location: String? = null,
    
    // Vaccine specific fields
    val disease: String? = null,
    val doseNumber: Int? = null,
    val totalDoses: Int? = null,
    val vaccineType: String? = null,
    val scheduledAgeMonths: Int? = null,
    val scheduledAgeDays: Int? = null,
    val notes: String? = null
)
