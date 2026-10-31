package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.data.util.SyncManager
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.ChartNormDao
import com.misterio.core.database.dao.ChildDao
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.database.entity.MeasurementEntity
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.DomainError
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.domain.sync.SyncResult
import com.misterio.core.network.api.MeasurementApi
import com.misterio.core.network.dto.AddMeasurementRequest
import com.misterio.curakid.core.common.AppDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementRepositoryImpl @Inject constructor(
    private val measurementApi: MeasurementApi,
    private val measurementDao: MeasurementDao,
    private val childDao: ChildDao,
    private val chartNormDao: ChartNormDao,
    private val appDataStore: AppDataStore,
    private val syncManager: SyncManager,
) : MeasurementRepository {

    override fun getMeasurementsForChild(childId: UUID): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsForChild(childId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncMeasurementsForChild(childId: UUID, force: Boolean): Result<Unit> {
        return safeApiCall { measurementApi.getMeasurements(childId) }.map { page ->
            val dtos = page.content
            Log.d("MEASUREMENT", "SYNC MEASUREMENT FOR $childId")
            Log.d("MEASUREMENT", "DTOs: $dtos")
            measurementDao.insertMeasurements(dtos.map { it.toEntity() })
            Unit
        }
    }

    override suspend fun addMeasurement(
        childId: UUID,
        weight: Double?,
        height: Double?,
        headCirc: Double?,
    ): Result<Unit> {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        val tempEntity = MeasurementEntity(
            id = id,
            childId = childId,
            weightKg = weight,
            heightCm = height,
            headCircCm = headCirc,
            measurementDate = now,
            aiReportId = null,
            isSynced = false,
        )

        // 1. Save locally first (optimistic UI)
        measurementDao.insertMeasurement(tempEntity)

        // 2. Direct online API call first (as in assessment flow)
        val request = com.misterio.core.network.dto.AddMeasurementRequest(
            id = id,
            weightKg = weight,
            heightCm = height,
            headCircCm = headCirc,
            measurementDate = now.atZone(ZoneId.systemDefault()).toInstant().toString()
        )
        return safeApiCall { measurementApi.addMeasurement(childId, request) }
            .onSuccess { savedDto ->
                measurementDao.insertMeasurement(savedDto.toEntity())
            }
            .onFailure {
                // If offline or network error, schedule background worker
                syncManager.scheduleSync()
            }
            .map { Unit }
            .recover {
                // Keep optimistic success so UI updates smoothly
                Unit
            }
    }

    override suspend fun getMeasurementNorms(
        chartType: ChartType,
        gender: Gender
    ): Result<List<ChartNormPoint>> {
        Log.d(TAG, "getting meas. norms")
        val lastSync = appDataStore.getLastNormsSyncTime(chartType.name, gender.name)
        val currentTime = System.currentTimeMillis()
        val isExpired = currentTime - lastSync > MEASUREMENT_NORMS_LIFETIME

        val cachedNorms = chartNormDao.getNorms(chartType.name, gender.name)

        if (cachedNorms.isNotEmpty() && !isExpired) {
            Log.d(TAG, "CACHE IS fresh")
            return Result.success(cachedNorms.map { it.toDomain() })
        }

        return safeApiCall { measurementApi.getMeasurementNorms(chartType.name, gender.name) }
            .map { body ->
                chartNormDao.insertNorms(body.map { it.toEntity(chartType, gender) })
                appDataStore.saveLastNormsSyncTime(chartType.name, gender.name, currentTime)
                body
            }
            .recover { error ->
                if (cachedNorms.isNotEmpty()) {
                    cachedNorms.map { it.toDomain() }
                } else {
                    throw error
                }
            }
    }

    override suspend fun syncPending(): SyncResult {
        val unsynced = measurementDao.getUnsyncedMeasurements()
        if (unsynced.isEmpty()) return SyncResult.Success

        var transientFailure: Throwable? = null
        var permanentFailure: Throwable? = null

        for (measurement in unsynced) {
            val child = childDao.getChildById(measurement.childId)
            if (child == null || !child.isSynced) {
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
                    measurementDao.insertMeasurement(measurement.copy(isSynced = true))
                } else if (error is DomainError.NetworkError || error is DomainError.ServerError) {
                    transientFailure = error
                } else {
                    permanentFailure = error
                }
            }
        }

        return when {
            transientFailure != null -> SyncResult.TransientFailure(transientFailure!!)
            permanentFailure != null -> SyncResult.PermanentFailure(permanentFailure!!)
            else -> SyncResult.Success
        }
    }

    private companion object {
        private const val TAG = "MeasurementRepository"
        private const val MEASUREMENT_NORMS_LIFETIME = 24 * 60 * 60 * 1000 // 24h
    }
}
