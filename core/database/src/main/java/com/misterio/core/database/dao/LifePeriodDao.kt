package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.misterio.core.database.entity.LifePeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LifePeriodDao {

    @Query("SELECT * FROM life_periods")
    fun getLifePeriods(): Flow<List<LifePeriodEntity>>

    @Query("SELECT * FROM life_periods WHERE code = :code")
    fun getLifePeriodByCode(code: String): Flow<LifePeriodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifePeriods(periods: List<LifePeriodEntity>)

}
