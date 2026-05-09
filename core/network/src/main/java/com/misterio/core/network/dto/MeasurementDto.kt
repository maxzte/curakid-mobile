package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName

// Для POST запиту
data class AddMeasurementRequest(
    @SerializedName("weightKg") val weightKg: Double?,
    @SerializedName("heightCm") val heightCm: Double?,
    @SerializedName("headCircCm") val headCircCm: Double?,
    @SerializedName("measurementDate") val measurementDate: String
)

// Для GET відповіді
data class MeasurementDto(
    @SerializedName("id") val id: String,
    @SerializedName("childId") val childId: String,
    @SerializedName("weightKg") val weightKg: Double?,
    @SerializedName("heightCm") val heightCm: Double?,
    @SerializedName("headCircCm") val headCircCm: Double?,
    @SerializedName("wfaZscore") val wfaZscore: Double?,
    @SerializedName("hfaZscore") val hfaZscore: Double?,
    @SerializedName("bmiZscore") val bmiZscore: Double?,
    @SerializedName("measurementDate") val measurementDate: String
)