package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.dao.LifePeriodDao
import com.misterio.core.database.entity.ChildSkillAssessmentEntity
import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.AssessmentAnswer
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.repository.AssessmentRepository
import com.misterio.core.network.api.AssessmentApi
import com.misterio.core.network.dto.AnswerRequest
import com.misterio.core.network.dto.CompletePeriodAssessmentRequest
import com.misterio.core.network.dto.SaveAnswersRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Instant

@Singleton
class AssessmentRepositoryImpl @Inject constructor(
    private val api: AssessmentApi,
    private val dao: AssessmentDao,
    private val lifePeriodDao: LifePeriodDao,
) : AssessmentRepository {

    override fun getChildAssessments(
        childId: String
    ): Flow<List<ChildAssessment>> {
        return dao.getChildAssessments(childId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncChildAssessments(childId: String): Result<Unit> {
        return safeApiCall { api.getAssessments(childId) }.map { body ->
            Log.d(TAG, "Syncing $body assessments for child $childId")
            if (body.isNotEmpty()) {
                dao.insertChildAssessments(body.map { it.toEntity() })
            } else {
                Log.d(TAG, "Server returned empty list, clearing local database for $childId")
                dao.clearChildAssessment(childId)
            }
            Unit
        }
    }

    override suspend fun saveAnswerForChild(
        periodId: String,
        childId: String,
        answer: Answer
    ): Result<ChildAssessment> {
        // 1. Try to reconstruct answers to avoid flickering
        val existingAssessment = dao.getChildAssessments(childId).map { list ->
            list.find { it.id == periodId }
        }.firstOrNull()

        val lifePeriodEntity = lifePeriodDao.getLifePeriodByCode(periodId).firstOrNull()
        val lifePeriod = lifePeriodEntity?.toDomain()

        val tempAnswers = existingAssessment?.answers?.toMutableList() ?: mutableListOf()
        val question = lifePeriod?.questions?.find { it.id == answer.questionId }
        val option = question?.options?.find { it.id == answer.selectedOptionId }

        if (question != null && option != null) {
            tempAnswers.removeAll { it.question?.id == answer.questionId }
            tempAnswers.add(
                AssessmentAnswer(
                    id = UUID.randomUUID().toString(),
                    question = question,
                    selectedOption = option,
                    answeredAt = Clock.System.now()
                )
            )
        }

        val tempEntity = ChildSkillAssessmentEntity(
            id = periodId,
            childId = childId,
            answers = tempAnswers,
            assessmentDate = Clock.System.now().toString(),
            completedAt = existingAssessment?.completedAt,
            isSynced = false
        )
        dao.insertChildAssessment(tempEntity)

        return safeApiCall {
            val request = SaveAnswersRequest(answers = listOf(answer.toDto()))
            api.saveAssessmentAnswers(childId, periodId, request)
        }.map { body ->
            val savedDto = body.last()
            dao.insertChildAssessment(savedDto.toEntity())
            savedDto.toDomain()
        }
    }

    override suspend fun updateAnswerForChild(
        childId: String,
        skillAssessmentId: String,
        answer: Answer
    ): Result<ChildAssessment> {
        val originalEntity = dao.getChildAssessmentById(skillAssessmentId)
        
        // Optimistic update
        originalEntity?.let { entity ->
            val updatedAnswers = entity.answers.toMutableList()
            val lifePeriodEntity = lifePeriodDao.getLifePeriodByCode(entity.id).firstOrNull()
            val lifePeriod = lifePeriodEntity?.toDomain()
            val question = lifePeriod?.questions?.find { it.id == answer.questionId }
            val option = question?.options?.find { it.id == answer.selectedOptionId }
            
            if (question != null && option != null) {
                updatedAnswers.removeAll { it.question?.id == answer.questionId }
                updatedAnswers.add(
                    AssessmentAnswer(
                        id = UUID.randomUUID().toString(),
                        question = question,
                        selectedOption = option,
                        answeredAt = Clock.System.now()
                    )
                )
                dao.insertChildAssessment(entity.copy(answers = updatedAnswers, isSynced = false))
            }
        }

        return safeApiCall { api.updateAssessmentAnswers(childId, skillAssessmentId, answer.toDto()) }
            .onSuccess { updatedDto ->
                dao.insertChildAssessment(updatedDto.toEntity())
            }
            .onFailure {
                originalEntity?.let { dao.insertChildAssessment(it) }
            }
            .map { it.toDomain() }
    }

    override suspend fun completePeriod(
        childId: String,
        periodCode: String,
        allowEarlyTransition: Boolean
    ): Result<CompletePeriodResult> {
        return safeApiCall {
            val request = CompletePeriodAssessmentRequest(periodCode, allowEarlyTransition)
            api.completeAssessment(childId, request)
        }.map { body ->
            // After completion, assessments might change, so we sync them
            dao.insertChildAssessments(body.assessments.map { it.toEntity() })
            CompletePeriodResult(
                periodCode = body.periodCode,
                completionPercent = body.completionPercent,
                requiredPercent = body.requiredPercent,
                completedAt = body.completedAt,
                assessments = body.assessments.map {
                    it.toDomain().copy(completedAt = body.completedAt)
                },
                reportId = body.reportId,
            )
        }
    }

    // Mapping extensions - DELETED, moved to AssessmentMapper.kt

    private fun Answer.toDto() = AnswerRequest(
        questionId = questionId,
        selectedOptionId = selectedOptionId,
        answeredAt = Clock.System.now().toString(),
    )

    private companion object {
        private const val TAG = "AssessmentRepository"
    }
}
