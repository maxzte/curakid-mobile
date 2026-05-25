package com.misterio.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ChildAssessment(
    val id: String,
    val childId: String,
    val answers: List<AssessmentAnswer>,
    val assessmentDate: String,
    val completedAt: String? = null
)

@Serializable
data class AssessmentAnswer(
    val id: String,
    val question: Question?,
    val selectedOption: Option?,
    val answeredAt: Instant,
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
