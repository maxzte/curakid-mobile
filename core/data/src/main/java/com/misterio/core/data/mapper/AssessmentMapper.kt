package com.misterio.core.data.mapper

import com.misterio.core.database.entity.ChildSkillAssessmentEntity
import com.misterio.core.domain.model.AssessmentAnswer
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.DomainReadiness
import com.misterio.core.domain.model.PeriodReadiness
import com.misterio.core.network.dto.AssessmentAnswerResponse
import com.misterio.core.network.dto.AssessmentResponse
import com.misterio.core.network.dto.DomainReadinessResponse
import com.misterio.core.network.dto.PeriodReadinessResponse
import java.time.Instant.parse
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Instant

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    .withZone(ZoneId.systemDefault())

fun AssessmentResponse.toDomain() = ChildAssessment(
    id = id,
    childId = childId,
    answers = answers.map { it.toDomain() },
    assessmentDate = try {
        dateFormatter.format(parse(assessmentDate))
    } catch (e: Exception) {
        assessmentDate
    },
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
    assessmentDate = try {
        dateFormatter.format(parse(assessmentDate))
    } catch (e: Exception) {
        assessmentDate
    },
    completedAt = completedAt?.let {
        try {
            dateFormatter.format(parse(it))
        } catch (e: Exception) {
            it
        }
    }
)

fun AssessmentAnswerResponse.toDomain() = AssessmentAnswer(
    id = id,
    question = question?.toDomain(),
    selectedOption = selectedOption?.toDomain(),
    answeredAt = Instant.parse(answeredAt),
)

fun PeriodReadinessResponse.toDomain() = PeriodReadiness(
    isReady = isReady,
    completionPercent = completionPercent,
    requiredPercent = requiredPercent,
    domainStatus = domainStatus.map { it.toDomain() },
    reasons = reasons,
    canTransitionEarly = canTransitionEarly
)

fun DomainReadinessResponse.toDomain() = DomainReadiness(
    domain = domain,
    completionPercent = completionPercent,
    isReady = isReady,
    requiredPercent = requiredPercent
)
