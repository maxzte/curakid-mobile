package com.misterio.core.domain.model

data class ChildAssessment(
    val id: String,
    val childId: String,
    val domain: String,
    val score: Double?,
    val rawData: Map<String, Any>,
    val assessmentDate: String
)

data class CompletePeriodResult(
    val periodCode: String,
    val completionPercent: Int,
    val requiredPercent: Int,
    val completedAt: String?,
    val assessments: List<ChildAssessment>
)
