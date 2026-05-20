package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.misterio.core.database.entity.ChartNormEntity

@Dao
interface ChartNormDao {

    @Query("SELECT * FROM chart_norms WHERE chartType = :type AND gender = :gender ORDER BY x ASC")
    suspend fun getNorms(type: String, gender: String): List<ChartNormEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNorms(norms: List<ChartNormEntity>)

    @Query("DELETE FROM chart_norms WHERE chartType = :type AND gender = :gender")
    suspend fun deleteNorms(type: String, gender: String)

}

