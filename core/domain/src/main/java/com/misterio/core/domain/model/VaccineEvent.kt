package com.misterio.core.domain.model

import java.time.LocalDateTime

data class VaccineEvent(
    val id: String,
    val name: String,
    val status: EventStatus,
    val date: LocalDateTime,
    val disease: String?,
    val doseNumber: Int?,
    val totalDoses: Int?,
    val type: VaccineType,
    val scheduledAgeMonths: Int?,
    val scheduledAgeDays: Int?,
    val notes: String?,
)

enum class VaccineType {
    VACCINATION,
    REVACCINATION
}
