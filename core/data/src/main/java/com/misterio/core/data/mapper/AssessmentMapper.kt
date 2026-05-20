package com.misterio.core.data.mapper

import com.misterio.core.database.entity.ChildSkillAssessmentEntity
import com.misterio.core.domain.model.AssessmentAnswer
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.network.dto.AssessmentAnswerResponse
import com.misterio.core.network.dto.AssessmentResponse
import java.time.LocalDateTime

fun AssessmentResponse.toDomain() = ChildAssessment(
    id = id,
    childId = childId,
    answers = answers.map { it.toDomain() },
    assessmentDate = assessmentDate,
    completedAt = null
)

fun AssessmentResponse.toEntity() = ChildSkillAssessmentEntity(
    id = id,
    childId = childId,
    answers = answers.map { it.toDomain() },
    assessmentDate = assessmentDate,
    completedAt = null,
    isSynced = true
)

fun ChildSkillAssessmentEntity.toDomain() = ChildAssessment(
    id = id,
    childId = childId,
    answers = answers,
    assessmentDate = assessmentDate,
    completedAt = completedAt
)

fun AssessmentAnswerResponse.toDomain() = AssessmentAnswer(
    id = id,
    question = question?.toDomain(),
    selectedOption = selectedOption?.toDomain(),
    answeredAt = LocalDateTime.parse(answeredAt),
)
