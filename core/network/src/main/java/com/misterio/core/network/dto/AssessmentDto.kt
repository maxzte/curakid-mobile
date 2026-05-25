package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName


data class AssessmentResponse(
    @SerializedName("assessmentId") val id: String,
    @SerializedName("childId") val childId: String,
    @SerializedName("answers") val answers: List<AssessmentAnswerResponse>,
    @SerializedName("assessmentDate") val assessmentDate: String
)

data class AssessmentAnswerResponse(
    @SerializedName("assessmentAnswerId") val id: String,
    @SerializedName("question") var question: QuestionResponse?,
    @SerializedName("selectedOption") var selectedOption: OptionResponse?,
    @SerializedName("answeredAt") var answeredAt: String,
)

data class SaveAnswersRequest(
    @SerializedName("answers") val answers: List<AnswerRequest>,
    @SerializedName("assessmentDate") val assessmentDate: String? = null
)

data class AnswerRequest(
    @SerializedName("questionId") val questionId: String,
    @SerializedName("selectedOptionId") val selectedOptionId: String,
    @SerializedName("answeredAt") val answeredAt: String? = null,
)

data class CompletePeriodAssessmentRequest(
    @SerializedName("periodCode") val periodCode: String,
    @SerializedName("allowEarlyTransition") val allowEarlyTransition: Boolean = false
)

data class CompletePeriodAssessmentResponse(
    @SerializedName("periodCode") val periodCode: String,
    @SerializedName("completionPercent") val completionPercent: Int,
    @SerializedName("requiredPercent") val requiredPercent: Int,
    @SerializedName("completedAt") val completedAt: String,
    @SerializedName("assessments") val assessments: List<AssessmentResponse>,
    @SerializedName("reportId") val reportId: String? = null,
)
