package com.misterio.core.network.dto

data class VaccineDto(
    val id: String,
    val name: String,
    val disease: String,
    val doseNumber: Int,
    val totalDoses: Int,
    val type: String,
    val scheduledAgeMonths: Int?,
    val scheduledAgeDays: Int?,
    val administeredDate: String?,
    val batchNumber: String?,
    val administeredBy: String?,
    val notes: String?,
    val status: String
)

data class UpdateVaccineRequest(
    val status: String,
    val administeredDate: String?,
    val batchNumber: String?,
    val administeredBy: String?,
    val notes: String?
)
