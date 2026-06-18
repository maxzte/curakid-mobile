package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class AddMeasurementRequest(
    val id: UUID,
    @SerializedName("weightKg") val weightKg: Double?,
    @SerializedName("heightCm") val heightCm: Double?,
    @SerializedName("headCircCm") val headCircCm: Double?,
    @SerializedName("measurementDate") val measurementDate: String
)

data class MeasurementResponse(
    @SerializedName("id") val id: UUID,
    @SerializedName("childId") val childId: UUID,
    @SerializedName("measurementDate") val measurementDate: String,
    @SerializedName("aiReportId") val aiReportId: UUID?,

    @SerializedName("weightKg") val weightKg: Double?,
    @SerializedName("heightCm") val heightCm: Double?,
    @SerializedName("headCircCm") val headCircCm: Double?,

    @SerializedName("wfaZscore") val wfaZscore: Double?,
    @SerializedName("hfaZscore") val hfaZscore: Double?,
    @SerializedName("wfhZscore") val wfhZscore: Double?,
    @SerializedName("bmiZscore") val bmiZscore: Double?,
    @SerializedName("hcfaZscore") val hcfaZscore: Double?,

    @SerializedName("wfaPercentile") val wfaPercentile: Double?,
    @SerializedName("hfaPercentile") val hfaPercentile: Double?,
    @SerializedName("bmiPercentile") val bmiPercentile: Double?,
    @SerializedName("hcfaPercentile") val hcfaPercentile: Double?,
    @SerializedName("wfhPercentile") val wfhPercentile: Double?,
)
