package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName

data class LifePeriodDto(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("label") val label: String,
    @SerializedName("about") val about: String,
    @SerializedName("minMonths") val minMonths: Int,
    @SerializedName("maxMonths") val maxMonths: Int,
    @SerializedName("questions") val questions: List<QuestionDto>,
)

data class QuestionDto(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("options") val options: List<OptionDto>,
)

data class OptionDto(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: String,
)

data class AssessmentResponse(
    @SerializedName("id") val id: String,
    @SerializedName("childId") val childId: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("score") val score: Double?,
    @SerializedName("rawData") val rawData: Map<String, Any>?,
    @SerializedName("assessmentDate") val assessmentDate: String
)

data class SaveAssessmentRequest(
    @SerializedName("domain") val domain: String,
    @SerializedName("rawData") val rawData: Map<String, Any>?,
    @SerializedName("assessmentDate") val assessmentDate: String? = null
)

data class CompletePeriodAssessmentRequest(
    @SerializedName("periodCode") val periodCode: String,
    @SerializedName("allowEarlyTransition") val allowEarlyTransition: Boolean = false
)

data class CompletePeriodAssessmentResponse(
    @SerializedName("periodCode") val periodCode: String,
    @SerializedName("completionPercent") val completionPercent: Int,
    @SerializedName("requiredPercent") val requiredPercent: Int,
    @SerializedName("completedAt") val completedAt: String?,
    @SerializedName("assessments") val assessments: List<AssessmentResponse>
)

data class CompletionErrorDto(
    @SerializedName("message") val message: String,
    @SerializedName("completionPercent") val completionPercent: Int?,
    @SerializedName("requiredPercent") val requiredPercent: Int?,
    @SerializedName("reason") val reason: String?
)
