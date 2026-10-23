package com.misterio.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.data.mapper.toUpdateRequest
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.AssessmentDao
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.dao.EventDao
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.model.EventType
import com.misterio.core.network.api.AssessmentApi
import com.misterio.core.network.api.ChildApi
import com.misterio.core.network.api.EventApi
import com.misterio.core.network.api.MeasurementApi
import com.misterio.core.network.dto.AddMeasurementRequest
import com.misterio.core.network.dto.AnswerRequest
import com.misterio.core.network.dto.CreateChildRequest
import com.misterio.core.network.dto.SaveAnswersRequest
import com.misterio.core.network.dto.toRequest
import com.misterio.core.network.dto.toUpdateRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.ZoneId
import java.util.UUID

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val childDao: ChildDao,
    private val measurementDao: MeasurementDao,
    private val eventDao: EventDao,
    private val assessmentDao: AssessmentDao,
    private val childApi: ChildApi,
    private val measurementApi: MeasurementApi,
    private val eventApi: EventApi,
    private val assessmentApi: AssessmentApi,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync work")

        // 1. Sync children first as they are parents for other data
        val childrenSyncStatus = syncChildren()
        if (childrenSyncStatus is SyncStatus.PermanentFailure) {
            // If we can't sync children, we might need to stop or skip dependent data
            Log.e(TAG, "Permanent failure syncing children")
        }

        // 2. Sync other data only for already synced children
        val measurementsResult = syncMeasurements()
        val eventsResult = syncEvents()
        val assessmentsResult = syncAssessments()

        val results =
            listOf(childrenSyncStatus, measurementsResult, eventsResult, assessmentsResult)

        return when {
            results.any { it is SyncStatus.TransientFailure } -> {
                Log.d(TAG, "Sync work encountered transient failures, retrying...")
                Result.retry()
            }

            results.all { it is SyncStatus.Success || it is SyncStatus.PermanentFailure } -> {
                Log.d(TAG, "Sync work finished (success or permanent failures)")
                Result.success()
            }

            else -> Result.success()
        }
    }

    private sealed class SyncStatus {
        object Success : SyncStatus()
        object TransientFailure : SyncStatus()
        object PermanentFailure : SyncStatus()
    }

    private suspend fun syncChildren(): SyncStatus {
        val unsynced = childDao.getUnsyncedChildren()
        if (unsynced.isEmpty()) return SyncStatus.Success

        var hasTransientFailure = false
        var hasPermanentFailure = false

        for (child in unsynced) {
            val request = CreateChildRequest(
                id = child.id,
                name = child.name,
                birthDate = child.birthDate.toString(),
                gender = child.gender,
                isPremature = child.isPremature,
                gestationalWeeks = child.gestationalWeeks,
                allergies = child.allergies,
                chronicDiseases = child.chronicDiseases,
                pastDiseases = child.pastDiseases,
                specialFeatures = child.specialFeatures,
            )
            val createResult = safeApiCall { childApi.createChild(request) }

            val result = if (createResult.isFailure && createResult.exceptionOrNull() is DomainError.BadRequestError) {
                // Already exists, try update
                safeApiCall { childApi.updateChild(child.id, child.toUpdateRequest()) }
            } else {
                createResult
            }

            result.onSuccess { createdChild ->
                childDao.insertChild(createdChild.toEntity())
            }.onFailure { error ->
                if (error is DomainError.NetworkError || error is DomainError.ServerError) {
                    hasTransientFailure = true
                } else {
                    hasPermanentFailure = true
                }
            }
        }

        return when {
            hasTransientFailure -> SyncStatus.TransientFailure
            hasPermanentFailure -> SyncStatus.PermanentFailure
            else -> SyncStatus.Success
        }
    }

    private suspend fun syncMeasurements(): SyncStatus {
        val unsynced = measurementDao.getUnsyncedMeasurements()
        if (unsynced.isEmpty()) return SyncStatus.Success

        var hasTransientFailure = false
        var hasPermanentFailure = false

        for (measurement in unsynced) {
            // Check if child is already synced
            val child = childDao.getChildById(measurement.childId)
            if (child == null || !child.isSynced) {
                // Skip if parent child not synced yet
                continue
            }

            val request = AddMeasurementRequest(
                id = measurement.id,
                weightKg = measurement.weightKg,
                heightCm = measurement.heightCm,
                headCircCm = measurement.headCircCm,
                measurementDate = measurement.measurementDate.atZone(ZoneId.systemDefault())
                    .toInstant().toString()
            )
            val result = safeApiCall { measurementApi.addMeasurement(measurement.childId, request) }

            result.onSuccess { savedDto ->
                measurementDao.insertMeasurement(savedDto.toEntity())
            }.onFailure { error ->
                if (error is DomainError.BadRequestError) {
                    // Likely duplicate ID, mark as synced by fetching or just marking
                    // For now, we assume it's synced if it's a 400 on creation
                    measurementDao.insertMeasurement(measurement.copy(isSynced = true))
                } else if (error is DomainError.NetworkError || error is DomainError.ServerError) {
                    hasTransientFailure = true
                } else {
                    hasPermanentFailure = true
                }
            }
        }
        return if (hasTransientFailure) SyncStatus.TransientFailure else if (hasPermanentFailure) SyncStatus.PermanentFailure else SyncStatus.Success
    }

    private suspend fun syncEvents(): SyncStatus {
        val unsynced = eventDao.getAllUnsyncedEvents()
        val pendingDeletes = eventDao.getPendingDeleteEvents()

        if (unsynced.isEmpty() && pendingDeletes.isEmpty()) return SyncStatus.Success

        var hasTransientFailure = false
        var hasPermanentFailure = false

        // Sync Deletions
        for (event in pendingDeletes) {
            val result = safeApiCall { eventApi.deleteEvent(event.id) }
            result.onSuccess {
                eventDao.hardDeleteEvent(event.id)
            }.onFailure { error ->
                if (error is DomainError.NetworkError || error is DomainError.ServerError) {
                    hasTransientFailure = true
                } else if (error is DomainError.NotFoundError) {
                    // Already deleted on server
                    eventDao.hardDeleteEvent(event.id)
                } else {
                    hasPermanentFailure = true
                }
            }
        }

        // Sync Creations/Updates
        for (event in unsynced) {
            val child = childDao.getChildById(event.childId)
            if (child == null || !child.isSynced) continue

            val domainEvent = event.toDomain()
            val result = if (domainEvent.type == EventType.VACCINE) {
                // Vaccines always exist on server (pre-populated), so we update
                safeApiCall {
                    eventApi.updateEvent(
                        event.childId,
                        event.id,
                        domainEvent.toUpdateRequest()
                    )
                }
            } else {
                // For USER events, we try to create, but if it fails with 400 (Duplicate), 
                // we should probably have updated it. 
                // However, a better way is to check if it's a known event.
                // For simplicity, we try to create and if it fails due to "duplicate key", we update.
                val createResult = safeApiCall {
                    eventApi.createEvents(
                        event.childId,
                        listOf(domainEvent.toRequest(event.childId))
                    )
                }
                
                if (createResult.isFailure && createResult.exceptionOrNull() is DomainError.BadRequestError) {
                   // Logically this could be a duplicate key error
                   safeApiCall {
                       eventApi.updateEvent(
                           event.childId,
                           event.id,
                           domainEvent.toUpdateRequest()
                       )
                   }
                } else {
                    createResult
                }
            }

            result.onSuccess {
                eventDao.markAsSynced(event.id)
            }.onFailure { error ->
                if (error is DomainError.NetworkError || error is DomainError.ServerError) {
                    hasTransientFailure = true
                } else {
                    hasPermanentFailure = true
                }
            }
        }
        return if (hasTransientFailure) SyncStatus.TransientFailure else if (hasPermanentFailure) SyncStatus.PermanentFailure else SyncStatus.Success
    }

    private suspend fun syncAssessments(): SyncStatus {
        val unsynced = assessmentDao.getUnsyncedAssessments()
        if (unsynced.isEmpty()) return SyncStatus.Success

        var hasTransientFailure = false
        var hasPermanentFailure = false

        for (assessment in unsynced) {
            val child = childDao.getChildById(assessment.childId)
            if (child == null || !child.isSynced) continue

            val answersRequests = assessment.answers.mapNotNull {
                val qId = it.question?.id ?: return@mapNotNull null
                val optId = it.selectedOption?.id ?: return@mapNotNull null
                AnswerRequest(
                    questionId = qId,
                    selectedOptionId = optId,
                    answeredAt = it.answeredAt.toString()
                )
            }

            val result = safeApiCall {
                assessmentApi.saveAssessmentAnswers(
                    assessment.childId,
                    assessment.id,
                    SaveAnswersRequest(answersRequests)
                )
            }

            result.onSuccess { responses ->
                val savedDto = responses.lastOrNull()
                if (savedDto != null) {
                    if (savedDto.id != assessment.id) {
                        assessmentDao.deleteChildAssessment(assessment.id)
                    }
                    assessmentDao.insertChildAssessment(savedDto.toEntity())
                } else {
                    assessmentDao.markAsSynced(assessment.id)
                }
            }.onFailure { error ->
                if (error is DomainError.NetworkError || error is DomainError.ServerError) {
                    hasTransientFailure = true
                } else {
                    hasPermanentFailure = true
                }
            }
        }
        return if (hasTransientFailure) SyncStatus.TransientFailure else if (hasPermanentFailure) SyncStatus.PermanentFailure else SyncStatus.Success
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}
