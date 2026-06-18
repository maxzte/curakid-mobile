package com.misterio.core.domain.model

import com.misterio.core.domain.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
data class ChildAssessment(
    @Serializable(UUIDSerializer::class) val id: UUID,
    @Serializable(UUIDSerializer::class) val childId: UUID,
    val answers: List<AssessmentAnswer>,
    val assessmentDate: String,
    val completedAt: String? = null
)

@Serializable
data class AssessmentAnswer(
    @Serializable(UUIDSerializer::class) val id: UUID,
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
    val reportId: UUID?,
)

data class Answer(
    val questionId: UUID,
    val selectedOptionId: UUID,
)
