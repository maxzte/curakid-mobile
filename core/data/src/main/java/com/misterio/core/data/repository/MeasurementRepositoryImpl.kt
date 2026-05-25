package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.data.util.safeApiCall
import com.misterio.curakid.core.common.AppDataStore
import com.misterio.core.database.dao.ChartNormDao
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.database.entity.ChartNormEntity
import com.misterio.core.database.entity.MeasurementEntity
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.network.api.MeasurementApi
import com.misterio.core.network.dto.AddMeasurementRequest
import com.misterio.core.network.dto.MeasurementResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
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
) : MeasurementRepository {

    override fun getMeasurementsForChild(childId: String): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsForChild(childId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncMeasurementsForChild(childId: String, force: Boolean): Result<Unit> {
//        val lastSync = settingsManager.getLastSyncTime(childId)
        val currentTime = System.currentTimeMillis()

        // Cache for 5 minutes (300,000 ms)
//        if (!force && (currentTime - lastSync < 300_000)) {
//            return Result.success(Unit)
//        }

        return safeApiCall { measurementApi.getMeasurements(childId) }.map { dtos ->
            Log.d("MEASUREMENT", "SYNC MEASUREMENT FOR $childId")
            Log.d("MEASUREMENT", "DTOs: $dtos")
            measurementDao.insertMeasurements(dtos.map { it.toEntity() })
//                settingsManager.saveLastSyncTime(childId, currentTime)
            Unit
        }
    }

    override suspend fun addMeasurement(
        childId: String,
        weight: Double?,
        height: Double?,
        headCirc: Double?,
    ): Result<Unit> {
        val tempId = UUID.randomUUID().toString()
        val tempEntity = MeasurementEntity(
            id = tempId,
            childId = childId,
            weightKg = weight,
            heightCm = height,
            headCircCm = headCirc,
            measurementDate = LocalDateTime.now(),
            aiReportId = null,
            isSynced = false,
        )

        // 1. Optimistic insert
        measurementDao.insertMeasurement(tempEntity)

        val dto = AddMeasurementRequest(
            weightKg = tempEntity.weightKg,
            heightCm = tempEntity.heightCm,
            headCircCm = tempEntity.headCircCm,
            measurementDate = tempEntity.measurementDate.atZone(ZoneId.systemDefault()).toInstant().toString()
        )

        return safeApiCall { measurementApi.addMeasurement(childId, dto) }.map { savedDto ->
            // 3. Replace local temp with real data from server
            measurementDao.deleteByLocalId(tempId)
            measurementDao.insertMeasurement(savedDto.toEntity())
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

    private fun ChartNormEntity.toDomain() = ChartNormPoint(
        x = x,
        p01 = p01,
        p1 = p1,
        p3 = p3,
        p5 = p5,
        p10 = p10,
        p15 = p15,
        p25 = p25,
        p50 = p50,
        p75 = p75,
        p85 = p85,
        p90 = p90,
        p95 = p95,
        p97 = p97,
        p99 = p99,
        p999 = p999
    )

    private fun ChartNormPoint.toEntity(type: ChartType, gender: Gender) = ChartNormEntity(
        chartType = type.name,
        gender = gender.name,
        x = x,
        p01 = p01,
        p1 = p1,
        p3 = p3,
        p5 = p5,
        p10 = p10,
        p15 = p15,
        p25 = p25,
        p50 = p50,
        p75 = p75,
        p85 = p85,
        p90 = p90,
        p95 = p95,
        p97 = p97,
        p99 = p99,
        p999 = p999
    )

    private fun MeasurementEntity.toDomain() = Measurement(
        id = id,
        childId = childId,
        date = measurementDate,
        aiReportId = aiReportId,

        weight = weightKg,
        height = heightCm,
        headCirc = headCircCm,

        wfaZscore = wfaZscore,
        hfaZscore = hfaZscore,
        wfhZscore = wfhZscore,
        bmiZscore = bmiZscore,
        hcfaZscore = hcfaZscore,

        wfaPercentile = wfaPercentile,
        hfaPercentile = hfaPercentile,
        wfhPercentile = wfhPercentile,
        bmiPercentile = bmiPercentile,
        hcfaPercentile = hcfaPercentile,

        isSynced = isSynced,
    )

    private fun MeasurementResponse.toEntity() = MeasurementEntity(
        id = id,
        childId = childId,
        measurementDate = Instant.parse(measurementDate).atZone(ZoneId.systemDefault()).toLocalDateTime(),
        aiReportId = aiReportId,

        weightKg = weightKg,
        heightCm = heightCm,
        headCircCm = headCircCm,

        wfaZscore = wfaZscore,
        hfaZscore = hfaZscore,
        wfhZscore = wfhZscore,
        bmiZscore = bmiZscore,
        hcfaZscore = hcfaZscore,

        wfaPercentile = wfaPercentile,
        hfaPercentile = hfaPercentile,
        wfhPercentile = wfhPercentile,
        bmiPercentile = bmiPercentile,
        hcfaPercentile = hcfaPercentile,

        isSynced = true,
    )

    private companion object {
        private const val TAG = "MeasurementRepository"
        private const val MEASUREMENT_NORMS_LIFETIME = 24 * 60 * 60 * 1000 // 24h
    }
}
