package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.misterio.core.database.entity.VaccineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineDao {

    @Query("SELECT * FROM vaccines")
    fun getAllVaccines(): Flow<List<VaccineEntity>>

    //    @Query("SELECT * FROM children WHERE id = :childId")
//    suspend fun getChildById(childId: String): ChildEntity?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertChild(child: ChildEntity)
//
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccines(vaccines: List<VaccineEntity>)
//
//    @Delete
//    suspend fun deleteChild(child: ChildEntity)
}
