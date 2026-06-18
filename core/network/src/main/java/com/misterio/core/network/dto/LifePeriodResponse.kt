package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class LifePeriodResponse(
    @SerializedName("id") val id: UUID,
    @SerializedName("code") val code: String,
    @SerializedName("label") val label: String,
    @SerializedName("description") val description: LifePeriodDescriptionResponse,
    @SerializedName("minMonths") val minMonths: Int,
    @SerializedName("maxMonths") val maxMonths: Int,
    @SerializedName("questions") val questions: List<QuestionResponse>,
)

data class LifePeriodDescriptionResponse(
    val title: String,
    val subtitle: String,
    val milestones: LifePeriodMilestonesResponse,
    val tips: List<String>
)

data class LifePeriodMilestonesResponse(
    val movement: String,
    val speech: String,
    val cognition: String
)
