package com.misterio.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.repository.AssessmentRepository
import com.misterio.core.domain.repository.ChildRepository
import com.misterio.core.domain.repository.DevelopmentRepository
import com.misterio.core.domain.repository.EventRepository
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.sync.SyncResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val developmentRepository: DevelopmentRepository,
    private val childRepository: ChildRepository,
    private val measurementRepository: MeasurementRepository,
    private val eventRepository: EventRepository,
    private val assessmentRepository: AssessmentRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync work")

        // 1. Sync catalog / reference data (life periods & milestone questions)
        val catalogSyncResult = syncCatalog()

        // 2. Sync children first as they are parents for other data
        val childrenSyncResult = childRepository.syncPending()
        if (childrenSyncResult is SyncResult.PermanentFailure) {
            Log.e(TAG, "Permanent failure syncing children", childrenSyncResult.error)
        }

        // 3. Sync dependent domain entities
        val measurementsResult = measurementRepository.syncPending()
        val eventsResult = eventRepository.syncPending()
        val assessmentsResult = assessmentRepository.syncPending()

        val results = listOf(
            catalogSyncResult,
            childrenSyncResult,
            measurementsResult,
            eventsResult,
            assessmentsResult,
        )

        return when {
            results.any { it is SyncResult.TransientFailure } -> {
                Log.d(TAG, "Sync work encountered transient failures, retrying...")
                Result.retry()
            }

            results.all { it is SyncResult.Success || it is SyncResult.PermanentFailure } -> {
                Log.d(TAG, "Sync work finished (success or permanent failures)")
                Result.success()
            }

            else -> Result.success()
        }
    }

    private suspend fun syncCatalog(): SyncResult {
        val result = developmentRepository.syncLifePeriods(force = true)
        return if (result.isSuccess) {
            SyncResult.Success
        } else {
            val error = result.exceptionOrNull() ?: DomainError.UnknownError()
            if (error is DomainError.NetworkError || error is DomainError.ServerError) {
                SyncResult.TransientFailure(error)
            } else {
                SyncResult.PermanentFailure(error)
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}
