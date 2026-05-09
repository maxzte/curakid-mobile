package com.misterio.core.domain.model

import java.time.LocalDate

data class Vaccine(
    val id: String,
    val name: String,
    val disease: String,
    val doseNumber: Int,
    val totalDoses: Int,
    val type: VaccineType,
    val scheduledAgeMonths: Int?,
    val scheduledAgeDays: Int?,
    val administeredDate: LocalDate?,
    val batchNumber: String?,
    val administeredBy: String?,
    val notes: String?,
    val status: VaccineStatus
)

enum class VaccineType {
    VACCINATION,     // перша вакцинація
    REVACCINATION    // ревакцинація
}

enum class VaccineStatus {
    PLANNED,    // заплановано за календарем
    DONE,       // зроблено
    SKIPPED,    // пропущено
    DELAYED     // відкладено
}
