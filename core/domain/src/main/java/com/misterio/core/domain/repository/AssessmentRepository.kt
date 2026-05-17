package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.CompletePeriodResult
import kotlinx.coroutines.flow.Flow

interface AssessmentRepository {

    fun getChildAssessments(childId: String): Flow<List<ChildAssessment>>

    suspend fun syncChildAssessments(childId: String): Result<Unit>

    suspend fun saveAnswerForChild(
        periodId: String,
        childId: String,
        answer: Answer,
    ): Result<ChildAssessment>

    suspend fun updateAnswerForChild(
        childId: String,
        skillAssessmentId: String,
        answer: Answer,
    ): Result<ChildAssessment>

    suspend fun completePeriod(
        childId: String,
        periodCode: String,
        allowEarlyTransition: Boolean,
    ): Result<CompletePeriodResult>

}
