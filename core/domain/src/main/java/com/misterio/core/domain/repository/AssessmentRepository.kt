package com.misterio.core.domain.repository

import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.Milestone
import kotlinx.coroutines.flow.Flow

interface AssessmentRepository {
    // Existing methods (keep for now to avoid breaking other features, but may need migration)
    fun getMilestones(childId: String): Flow<List<Milestone>>
    fun getMilestonesForAge(childId: String, ageInMonths: Int): Flow<List<Milestone>>
    suspend fun saveAssessment(childId: String, milestones: List<Milestone>)
    suspend fun syncAssessments(childId: String)
    suspend fun syncAssessmentsForAge(childId: String, ageInMonths: Int)

    // New methods for Life Period Quiz
    suspend fun getLifePeriods(): Result<List<LifePeriod>>
    suspend fun getLifePeriod(code: String): Result<LifePeriod>
    suspend fun getChildAssessments(childId: String, domain: String? = null): Result<List<ChildAssessment>>
    suspend fun saveChildAssessment(childId: String, domain: String, rawData: Map<String, Any>): Result<ChildAssessment>
    suspend fun updateChildAssessment(childId: String, assessmentId: String, domain: String, rawData: Map<String, Any>): Result<ChildAssessment>
    suspend fun completePeriod(childId: String, periodCode: String, allowEarlyTransition: Boolean): Result<CompletePeriodResult>
}
