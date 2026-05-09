package com.misterio.core.database.dao

import androidx.room.*
import com.misterio.core.database.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE childId = :childId ORDER BY measurementDate DESC")
    fun getMeasurementsForChild(childId: String): Flow<List<MeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteByLocalId(id: String)

    @Delete
    suspend fun deleteMeasurement(measurement: MeasurementEntity)
}
