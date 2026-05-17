package com.misterio.core.domain.model

import com.misterio.core.domain.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ChildAssessment(
    val id: String,
    val childId: String,
    val answers: List<AssessmentAnswer>,
    val assessmentDate: String
)

@Serializable
data class AssessmentAnswer(
    val id: String,
    val question: Question?,
    val selectedOption: Option?,
    val answeredAt: @Serializable(with = LocalDateTimeSerializer::class) LocalDateTime,
)

data class CompletePeriodResult(
    val periodCode: String,
    val completionPercent: Int,
    val requiredPercent: Int,
    val completedAt: String?,
    val assessments: List<ChildAssessment>,
    val reportId: String?,
)

data class Answer(
    val questionId: String,
    val selectedOptionId: String,
)
