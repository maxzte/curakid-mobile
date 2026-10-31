package com.misterio.core.domain.repository

import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.sync.Syncable
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface MeasurementRepository : Syncable {

    fun getMeasurementsForChild(childId: UUID): Flow<List<Measurement>>

    suspend fun getMeasurementNorms(
        chartType: ChartType,
        gender: Gender
    ): Result<List<ChartNormPoint>>

    suspend fun syncMeasurementsForChild(childId: UUID, force: Boolean = false): Result<Unit>

    suspend fun addMeasurement(
        childId: UUID,
        weight: Double?,
        height: Double?,
        headCirc: Double?,
    ): Result<Unit>

}
