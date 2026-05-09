package com.misterio.core.domain.repository

import com.misterio.core.domain.model.Measurement
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {

    fun getMeasurementsForChild(childId: String): Flow<List<Measurement>>

    suspend fun syncMeasurementsForChild(childId: String, force: Boolean = false): Result<Unit>

    suspend fun addMeasurement(
        childId: String,
        weight: Double?,
        height: Double?,
        headCirc: Double?,
    ): Result<Unit>

}
