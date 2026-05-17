package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.misterio.core.domain.model.Question

@Entity(tableName = "life_periods")
data class LifePeriodEntity(
    @PrimaryKey val id: String,
    val code: String,
    val label: String,
    val about: String,
    val minMonths: Int,
    val maxMonths: Int,
    val questions: List<Question>
)
