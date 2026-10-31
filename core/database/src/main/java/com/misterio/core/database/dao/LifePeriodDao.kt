package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.misterio.core.database.entity.LifePeriodEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface LifePeriodDao {

    @Query("SELECT * FROM life_periods ORDER BY minMonths ASC")
    fun getLifePeriods(): Flow<List<LifePeriodEntity>>

    @Query("SELECT * FROM life_periods WHERE id = :id")
    fun getLifePeriodById(id: UUID): Flow<LifePeriodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifePeriods(periods: List<LifePeriodEntity>)

    @Query("DELETE FROM life_periods WHERE id NOT IN (:activeIds)")
    suspend fun deletePeriodsNotIn(activeIds: List<UUID>)

    @Query("DELETE FROM life_periods")
    suspend fun clearAll()

    @Transaction
    suspend fun syncWithServer(periods: List<LifePeriodEntity>) {
        if (periods.isEmpty()) {
            clearAll()
        } else {
            deletePeriodsNotIn(periods.map { it.id })
            insertLifePeriods(periods)
        }
    }

}
