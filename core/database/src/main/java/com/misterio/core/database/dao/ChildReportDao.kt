package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.misterio.core.database.entity.ChildReportEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ChildReportDao {

    @Query("SELECT * FROM child_reports WHERE id = :id")
    fun getReportById(id: UUID): Flow<ChildReportEntity?>

    @Query("SELECT * FROM child_reports WHERE childId = :childId")
    fun getReportBy(childId: UUID): Flow<ChildReportEntity?>

    @Query("SELECT * FROM child_reports WHERE childId = :childId ORDER BY createdAt DESC")
    fun getReports(childId: UUID): Flow<List<ChildReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ChildReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ChildReportEntity>)

    @Query("DELETE FROM child_reports WHERE childId = :childId")
    suspend fun deleteReport(childId: UUID)

}
