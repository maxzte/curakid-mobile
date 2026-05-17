package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName

data class LifePeriodResponse(
    @SerializedName("periodId") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("label") val label: String,
    @SerializedName("description") val description: String,
    @SerializedName("minMonths") val minMonths: Int,
    @SerializedName("maxMonths") val maxMonths: Int,
    @SerializedName("questions") val questions: List<QuestionResponse>,
)
