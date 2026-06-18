package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.model.PeriodReadiness
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface AssessmentRepository {

    fun getChildAssessments(childId: UUID): Flow<List<ChildAssessment>>

    suspend fun syncChildAssessments(childId: UUID): Result<Unit>

    suspend fun getPeriodReadiness(childId: UUID, periodCode: String): Result<PeriodReadiness>

    suspend fun saveAnswerForChild(
        periodId: UUID,
        childId: UUID,
        answer: Answer,
    ): Result<ChildAssessment>

    suspend fun updateAnswerForChild(
        childId: UUID,
        skillAssessmentId: UUID,
        answer: Answer,
    ): Result<ChildAssessment>

    suspend fun completePeriod(
        childId: UUID,
        periodCode: String,
        allowEarlyTransition: Boolean,
    ): Result<CompletePeriodResult>

}
