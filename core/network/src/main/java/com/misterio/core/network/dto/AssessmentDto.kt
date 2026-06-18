package com.misterio.core.network.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID


data class AssessmentResponse(
    @SerializedName("assessmentId") val id: UUID,
    @SerializedName("childId") val childId: UUID,
    @SerializedName("answers") val answers: List<AssessmentAnswerResponse>,
    @SerializedName("assessmentDate") val assessmentDate: String
)

data class AssessmentAnswerResponse(
    @SerializedName("assessmentAnswerId") val id: UUID,
    @SerializedName("question") var question: QuestionResponse?,
    @SerializedName("selectedOption") var selectedOption: OptionResponse?,
    @SerializedName("answeredAt") var answeredAt: String,
)

data class SaveAnswersRequest(
    @SerializedName("answers") val answers: List<AnswerRequest>,
    @SerializedName("assessmentDate") val assessmentDate: String? = null
)

data class AnswerRequest(
    @SerializedName("questionId") val questionId: UUID,
    @SerializedName("selectedOptionId") val selectedOptionId: UUID,
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
    @SerializedName("reportId") val reportId: UUID? = null,
)

data class PeriodReadinessResponse(
    @SerializedName("isReady") val isReady: Boolean,
    @SerializedName("completionPercent") val completionPercent: Int,
    @SerializedName("requiredPercent") val requiredPercent: Int,
    @SerializedName("domainStatus") val domainStatus: List<DomainReadinessResponse>,
    @SerializedName("reasons") val reasons: List<String> = emptyList(),
    @SerializedName("canTransitionEarly") val canTransitionEarly: Boolean = false
)

data class DomainReadinessResponse(
    @SerializedName("domain") val domain: String,
    @SerializedName("completionPercent") val completionPercent: Int,
    @SerializedName("isReady") val isReady: Boolean,
    @SerializedName("requiredPercent") val requiredPercent: Int
)
