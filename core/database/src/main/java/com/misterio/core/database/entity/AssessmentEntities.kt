package com.misterio.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.misterio.core.domain.model.AssessmentAnswer
import java.util.UUID

@Entity(
    tableName = "child_skill_assessments",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["childId"]),
        Index(value = ["childId", "periodId"], unique = true)
    ]
)
data class ChildSkillAssessmentEntity(
    @PrimaryKey val id: UUID,
    val childId: UUID,
    val periodId: UUID? = null,
    val answers: List<AssessmentAnswer>,
    val assessmentDate: String,
    val completedAt: String? = null,
    val isSynced: Boolean = true
)
