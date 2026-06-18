package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.misterio.core.domain.model.LifePeriodDescription
import com.misterio.core.domain.model.Question
import java.util.UUID

@Entity(tableName = "life_periods")
data class LifePeriodEntity(
    @PrimaryKey val id: UUID,
    val code: String,
    val label: String,
    val about: LifePeriodDescription?,
    val minMonths: Int,
    val maxMonths: Int,
    val questions: List<Question>
)
