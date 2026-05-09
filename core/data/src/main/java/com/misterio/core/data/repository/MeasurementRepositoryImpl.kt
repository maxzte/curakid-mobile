package com.misterio.core.data.repository

import android.util.Log
import com.misterio.core.common.SettingsManager
import com.misterio.core.database.dao.MeasurementDao
import com.misterio.core.database.entity.MeasurementEntity
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.repository.MeasurementRepository
import com.misterio.core.network.api.MeasurementApi
import com.misterio.core.network.dto.AddMeasurementRequest
import com.misterio.core.network.dto.MeasurementDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementRepositoryImpl @Inject constructor(
    private val measurementApi: MeasurementApi,
    private val measurementDao: MeasurementDao,
    private val settingsManager: SettingsManager
) : MeasurementRepository {

    override fun getMeasurementsForChild(childId: String): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsForChild(childId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncMeasurementsForChild(childId: String, force: Boolean): Result<Unit> {
        val lastSync = settingsManager.getLastSyncTime(childId)
        val currentTime = System.currentTimeMillis()

        // Cache for 5 minutes (300,000 ms)
//        if (!force && (currentTime - lastSync < 300_000)) {
//            return Result.success(Unit)
//        }

        return try {
            Log.d("MEASUREMENT", "SYNC MEASUREMENT FOR $childId")
            val response = measurementApi.getMeasurements(childId)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                Log.d("MEASUREMENT", "DTOs: ${response.body()}")
                measurementDao.insertMeasurements(dtos.map { it.toEntity() })
                settingsManager.saveLastSyncTime(childId, currentTime)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMeasurement(
        childId: String,
        weight: Double?,
        height: Double?,
        headCirc: Double?,
    ): Result<Unit> {
        println(headCirc)
        val localId = UUID.randomUUID().toString()
        val tempEntity = MeasurementEntity(
            id = localId,
            childId = childId,
            weightKg = weight,
            heightCm = height,
            headCircCm = headCirc,
            measurementDate = LocalDateTime.now(),
            isSynced = false,
        )

        return try {
            // 1. Optimistic insert
            measurementDao.insertMeasurement(tempEntity)

            val dto = AddMeasurementRequest(
                weightKg = tempEntity.weightKg,
                heightCm = tempEntity.heightCm,
                headCircCm = tempEntity.headCircCm,
                measurementDate = tempEntity.measurementDate.toString()
            )
            println(dto)

            // 2. Network request
            val response = measurementApi.addMeasurement(childId, dto)
            if (response.isSuccessful && response.body() != null) {
                val savedDto = response.body()!!
                // 3. Replace local temp with real data from server
                measurementDao.deleteByLocalId(localId)
                measurementDao.insertMeasurement(savedDto.toEntity())
                Result.success(Unit)
            } else {
                // Keep it in DB as unsynced
                Result.failure(Exception("Add measurement failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Keep it in DB as unsynced
            Result.failure(e)
        }
    }

    private fun MeasurementEntity.toDomain() = Measurement(
        id = id,
        childId = childId,
        weight = weightKg,
        height = heightCm,
        headCirc = headCircCm,
        wfaZscore = wfaZscore,
        hfaZscore = hfaZscore,
        bmiZscore = bmiZscore,
        date = measurementDate,
        isSynced = isSynced
    )

    private fun MeasurementDto.toEntity() = MeasurementEntity(
        id = id,
        childId = childId,
        weightKg = weightKg,
        heightCm = heightCm,
        headCircCm = headCircCm,
        wfaZscore = wfaZscore,
        hfaZscore = hfaZscore,
        bmiZscore = bmiZscore,
        measurementDate = LocalDateTime.parse(measurementDate),
        isSynced = true
    )
}
