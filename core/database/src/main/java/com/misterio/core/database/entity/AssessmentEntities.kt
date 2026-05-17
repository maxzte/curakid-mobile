package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.misterio.core.domain.model.AssessmentAnswer


@Entity(tableName = "child_skill_assessments")
data class ChildSkillAssessmentEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val answers: List<AssessmentAnswer>,
    val assessmentDate: String,
    val isSynced: Boolean = true
)
