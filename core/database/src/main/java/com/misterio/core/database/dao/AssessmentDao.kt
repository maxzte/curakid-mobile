package com.misterio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.misterio.core.database.entity.AssessmentEntity
import com.misterio.core.database.entity.ChildSkillAssessmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {

    @Query("SELECT * FROM assessments WHERE childId = :childId")
    fun getAssessments(childId: String): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE childId = :childId AND ageInMonths = :ageInMonths")
    fun getAssessmentsForAge(childId: String, ageInMonths: Int): Flow<List<AssessmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessments(assessments: List<AssessmentEntity>)

    @Query("DELETE FROM assessments WHERE childId = :childId")
    suspend fun deleteAssessmentsForChild(childId: String)

    @Query("DELETE FROM assessments WHERE childId = :childId AND ageInMonths = :ageInMonths")
    suspend fun deleteAssessmentsForChildAndAge(childId: String, ageInMonths: Int)

    // Child Assessments
    @Query("SELECT * FROM child_skill_assessments WHERE childId = :childId")
    fun getChildAssessments(childId: String): Flow<List<ChildSkillAssessmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildAssessment(assessment: ChildSkillAssessmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildAssessments(assessments: List<ChildSkillAssessmentEntity>)

    @Query("SELECT * FROM child_skill_assessments WHERE id = :id")
    suspend fun getChildAssessmentById(id: String): ChildSkillAssessmentEntity?

    @Query("DELETE FROM child_skill_assessments WHERE id = :id")
    suspend fun deleteChildAssessment(id: String)

    @Query("DELETE FROM child_skill_assessments WHERE childId = :childId")
    suspend fun clearChildAssessment(childId: String)

}
