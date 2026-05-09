package com.misterio.core.data.repository

import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.entity.AssessmentEntity
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.Milestone
import com.misterio.core.domain.model.MilestoneDomain
import com.misterio.core.domain.model.Option
import com.misterio.core.domain.model.Question
import com.misterio.core.domain.repository.AssessmentRepository
import com.misterio.core.network.api.AssessmentApi
import com.misterio.core.network.api.DevelopmentApi
import com.misterio.core.network.dto.AssessmentResponse
import com.misterio.core.network.dto.CompletePeriodAssessmentRequest
import com.misterio.core.network.dto.LifePeriodDto
import com.misterio.core.network.dto.OptionDto
import com.misterio.core.network.dto.QuestionDto
import com.misterio.core.network.dto.SaveAssessmentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AssessmentRepositoryImpl @Inject constructor(
    private val api: AssessmentApi,
    private val developmentApi: DevelopmentApi,
    private val dao: AssessmentDao
) : AssessmentRepository {

    override fun getMilestones(childId: String): Flow<List<Milestone>> {
        return dao.getAssessments(childId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMilestonesForAge(childId: String, ageInMonths: Int): Flow<List<Milestone>> {
        return dao.getAssessmentsForAge(childId, ageInMonths).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveAssessment(childId: String, milestones: List<Milestone>) {
        // Stub for existing method, will be migrated if needed
    }

    override suspend fun syncAssessments(childId: String) {
        val response = api.getAssessments(childId)
        if (response.isSuccessful) {
            response.body()?.let { dtos ->
                // This part is tricky because AssessmentEntity is different from AssessmentResponse
                // For now, let's keep it simple or migrate AssessmentEntity
            }
        }
    }

    override suspend fun syncAssessmentsForAge(childId: String, ageInMonths: Int) {
        // Stub for existing method
    }

    // New methods for Life Period Quiz

    override suspend fun getLifePeriods(): Result<List<LifePeriod>> {
        return try {
            val response = developmentApi.getLifePeriods()
            if (response.isSuccessful) {
                Result.success(response.body()?.map { it.toDomain() } ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch life periods: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLifePeriod(code: String): Result<LifePeriod> {
        return try {
            val response = developmentApi.getLifePeriod(code)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it.toDomain()) }
                    ?: Result.failure(Exception("Life period not found"))
            } else {
                Result.failure(Exception("Failed to fetch life period: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChildAssessments(childId: String, domain: String?): Result<List<ChildAssessment>> {
        return try {
            val response = api.getAssessments(childId, domain)
            if (response.isSuccessful) {
                Result.success(response.body()?.map { it.toDomain() } ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch child assessments: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveChildAssessment(
        childId: String,
        domain: String,
        rawData: Map<String, Any>
    ): Result<ChildAssessment> {
        return try {
            val request = SaveAssessmentRequest(domain = domain, rawData = rawData)
            val response = api.saveAssessment(childId, request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it.toDomain()) }
                    ?: Result.failure(Exception("Saved assessment response is empty"))
            } else {
                Result.failure(Exception("Failed to save assessment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChildAssessment(
        childId: String,
        assessmentId: String,
        domain: String,
        rawData: Map<String, Any>
    ): Result<ChildAssessment> {
        return try {
            val request = SaveAssessmentRequest(domain = domain, rawData = rawData)
            val response = api.updateAssessment(childId, assessmentId, request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it.toDomain()) }
                    ?: Result.failure(Exception("Updated assessment response is empty"))
            } else {
                Result.failure(Exception("Failed to update assessment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completePeriod(
        childId: String,
        periodCode: String,
        allowEarlyTransition: Boolean
    ): Result<CompletePeriodResult> {
        return try {
            val request = CompletePeriodAssessmentRequest(periodCode, allowEarlyTransition)
            val response = api.completeAssessment(childId, request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(CompletePeriodResult(
                        periodCode = body.periodCode,
                        completionPercent = body.completionPercent,
                        requiredPercent = body.requiredPercent,
                        completedAt = body.completedAt,
                        assessments = body.assessments.map { it.toDomain() }
                    ))
                } ?: Result.failure(Exception("Completion response is empty"))
            } else {
                // Here we could parse CompletionErrorDto if needed
                Result.failure(Exception("Failed to complete period: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mapping extensions

    private fun LifePeriodDto.toDomain() = LifePeriod(
        id = id,
        code = code,
        label = label,
        about = about,
        minMonths = minMonths,
        maxMonths = maxMonths,
        questions = questions.map { it.toDomain() }
    )

    private fun QuestionDto.toDomain() = Question(
        id = id,
        text = text,
        domain = domain,
        weight = weight,
        options = options.map { it.toDomain() }
    )

    private fun OptionDto.toDomain() = Option(
        id = id,
        text = text,
        value = value
    )

    private fun AssessmentResponse.toDomain() = ChildAssessment(
        id = id,
        childId = childId,
        domain = domain,
        score = score,
        rawData = rawData ?: emptyMap(),
        assessmentDate = assessmentDate
    )

    private fun AssessmentEntity.toDomain() = Milestone(
        id = id,
        text = text,
        domain = MilestoneDomain.valueOf(domain),
        ageInMonths = ageInMonths,
        isAchieved = achieved
    )
}
