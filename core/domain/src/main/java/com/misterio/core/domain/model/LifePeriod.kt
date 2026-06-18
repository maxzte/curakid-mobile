package com.misterio.core.domain.model

import com.misterio.core.domain.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class LifePeriod(
    @Serializable(UUIDSerializer::class) val id: UUID,
    val code: String,
    val label: String,
    val about: LifePeriodDescription?,
    val minMonths: Int,
    val maxMonths: Int,
    val questions: List<Question>
)
