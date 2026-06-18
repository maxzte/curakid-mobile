package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.mapper.toDomain
import com.misterio.core.data.mapper.toEntity
import com.misterio.core.data.util.SyncManager
import com.misterio.core.data.util.safeApiCall
import com.misterio.core.database.dao.ChartNormDao
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.database.entity.MeasurementEntity
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.network.api.MeasurementApi
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
        return safeApiCall { measurementApi.getMeasurements(childId) }.map { dtos ->
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
        val tempEntity = MeasurementEntity(
            id = id,
            childId = childId,
            weightKg = weight,
            heightCm = height,
            headCircCm = headCirc,
            measurementDate = LocalDateTime.now(),
            aiReportId = null,
            isSynced = false,
        )

        // 1. Save locally first
        measurementDao.insertMeasurement(tempEntity)
        syncManager.scheduleSync()

        return Result.success(Unit)
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

    private companion object {
        private const val TAG = "MeasurementRepository"
        private const val MEASUREMENT_NORMS_LIFETIME = 24 * 60 * 60 * 1000 // 24h
    }
}
