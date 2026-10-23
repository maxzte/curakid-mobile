package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.data.util.SyncManager
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.dao.LifePeriodDao
import com.misterio.core.database.entity.ChildSkillAssessmentEntity
import com.misterio.core.domain.model.Answer
import com.misterio.core.domain.model.AssessmentAnswer
import com.misterio.core.domain.model.ChildAssessment
import com.misterio.core.domain.model.CompletePeriodResult
import com.misterio.core.domain.model.PeriodReadiness
import com.misterio.core.domain.repository.AssessmentRepository
import com.misterio.core.network.api.AssessmentApi
import com.misterio.core.network.dto.AnswerRequest
import com.misterio.core.network.dto.CompletePeriodAssessmentRequest
import com.misterio.core.network.dto.SaveAnswersRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class AssessmentRepositoryImpl @Inject constructor(
    private val api: AssessmentApi,
    private val dao: AssessmentDao,
    private val lifePeriodDao: LifePeriodDao,
    private val syncManager: SyncManager,
) : AssessmentRepository {

    override fun getChildAssessments(
        childId: UUID
    ): Flow<List<ChildAssessment>> {
        return dao.getChildAssessments(childId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncChildAssessments(childId: UUID): Result<Unit> {
        return safeApiCall { api.getAssessments(childId) }.map { body ->
            Log.d(TAG, "Syncing ${body.size} assessments for child $childId")
            if (body.isNotEmpty()) {
                val unsyncedIds = dao.getUnsyncedAssessments().map { it.id }.toSet()
                val toInsert = body.map { it.toEntity() }.filterNot { it.id in unsyncedIds }
                if (toInsert.isNotEmpty()) {
                    dao.insertChildAssessments(toInsert)
                }
            }
            Unit
        }
    }

    override suspend fun getPeriodReadiness(
        childId: UUID,
        periodCode: String
    ): Result<PeriodReadiness> {
        return safeApiCall { api.getReadiness(childId, periodCode) }.map { it.toDomain() }
    }

    override suspend fun saveAnswerForChild(
        periodId: UUID,
        childId: UUID,
        answer: Answer
    ): Result<ChildAssessment> {
        // 1. Try to reconstruct answers to avoid flickering
        val existingAssessment = dao.getChildAssessments(childId).map { list ->
            list.find { it.id == periodId }
        }.firstOrNull()

        val lifePeriodEntity = lifePeriodDao.getLifePeriodById(periodId).firstOrNull()
        val lifePeriod = lifePeriodEntity?.toDomain()

        val tempAnswers = existingAssessment?.answers?.toMutableList() ?: mutableListOf()
        val question = lifePeriod?.questions?.find { it.id == answer.questionId }
        val option = question?.options?.find { it.id == answer.selectedOptionId }

        if (question != null && option != null) {
            tempAnswers.removeAll { it.question?.id == answer.questionId }
            tempAnswers.add(
                AssessmentAnswer(
                    id = UUID.randomUUID(),
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
            val savedDto = body.lastOrNull()
            if (savedDto != null) {
                if (savedDto.id != periodId) {
                    dao.deleteChildAssessment(periodId)
                }
                dao.insertChildAssessment(savedDto.toEntity())
                savedDto.toDomain()
            } else {
                tempEntity.toDomain()
            }
        }.recover {
            syncManager.scheduleSync()
            tempEntity.toDomain()
        }
    }

    override suspend fun updateAnswerForChild(
        childId: UUID,
        skillAssessmentId: UUID,
        answer: Answer
    ): Result<ChildAssessment> {
        val originalEntity = dao.getChildAssessmentById(skillAssessmentId)
        
        // Optimistic update
        var tempAssessment: ChildAssessment? = null
        originalEntity?.let { entity ->
            val updatedAnswers = entity.answers.toMutableList()
            val lifePeriods = lifePeriodDao.getLifePeriods().firstOrNull() ?: emptyList()
            val allQuestions = lifePeriods.flatMap { it.toDomain().questions }
            val question = allQuestions.find { it.id == answer.questionId }
            val option = question?.options?.find { it.id == answer.selectedOptionId }
            
            if (question != null && option != null) {
                updatedAnswers.removeAll { it.question?.id == answer.questionId }
                updatedAnswers.add(
                    AssessmentAnswer(
                        id = UUID.randomUUID(),
                        question = question,
                        selectedOption = option,
                        answeredAt = Clock.System.now()
                    )
                )
                val updatedEntity = entity.copy(answers = updatedAnswers, isSynced = false)
                dao.insertChildAssessment(updatedEntity)
                tempAssessment = updatedEntity.toDomain()
            }
        }

        return safeApiCall { api.updateAssessmentAnswers(childId, skillAssessmentId, answer.toDto()) }
            .onSuccess { updatedDto ->
                dao.insertChildAssessment(updatedDto.toEntity())
            }
            .onFailure {
                // Schedule sync only if network failed
                syncManager.scheduleSync()
            }
            .map { it.toDomain() }
            .recover {
                tempAssessment ?: throw it
            }
    }

    override suspend fun completePeriod(
        childId: UUID,
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
