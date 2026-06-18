package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.misterio.core.domain.model.AssessmentAnswer
import java.util.UUID


@Entity(tableName = "child_skill_assessments")
data class ChildSkillAssessmentEntity(
    @PrimaryKey val id: UUID,
    val childId: UUID,
    val answers: List<AssessmentAnswer>,
    val assessmentDate: String,
    val completedAt: String? = null,
    val isSynced: Boolean = true
)
