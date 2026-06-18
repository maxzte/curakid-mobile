package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.misterio.core.database.entity.LifePeriodEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface LifePeriodDao {

    @Query("SELECT * FROM life_periods")
    fun getLifePeriods(): Flow<List<LifePeriodEntity>>

    @Query("SELECT * FROM life_periods WHERE id = :id")
    fun getLifePeriodById(id: UUID): Flow<LifePeriodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifePeriods(periods: List<LifePeriodEntity>)

}
