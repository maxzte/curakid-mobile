package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "assessments")
data class AssessmentEntity(
    @PrimaryKey val id: UUID,
    val childId: UUID,
    val text: String,
    val domain: String,
    val ageInMonths: Int,
    val achieved: Boolean
)
