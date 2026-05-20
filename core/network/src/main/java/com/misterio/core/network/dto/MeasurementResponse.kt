package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName

data class AddMeasurementRequest(
    @SerializedName("weightKg") val weightKg: Double?,
    @SerializedName("heightCm") val heightCm: Double?,
    @SerializedName("headCircCm") val headCircCm: Double?,
    @SerializedName("measurementDate") val measurementDate: String
)

data class MeasurementResponse(
    @SerializedName("id") val id: String,
    @SerializedName("childId") val childId: String,
    @SerializedName("measurementDate") val measurementDate: String,
    @SerializedName("aiReportId") val aiReportId: String?,

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
