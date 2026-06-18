package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.misterio.core.database.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MeasurementDao {

    @Query("SELECT * FROM measurements WHERE childId = :childId ORDER BY measurementDate DESC")
    fun getMeasurementsForChild(childId: UUID): Flow<List<MeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteByLocalId(id: UUID)

    @Delete
    suspend fun deleteMeasurement(measurement: MeasurementEntity)

    @Query("SELECT * FROM measurements WHERE isSynced = 0")
    suspend fun getUnsyncedMeasurements(): List<MeasurementEntity>

    @Query("UPDATE measurements SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: UUID)

}
