package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assessments")
data class AssessmentEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val text: String,
    val domain: String,
    val ageInMonths: Int,
    val achieved: Boolean
)
