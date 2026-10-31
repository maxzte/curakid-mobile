package com.misterio.core.data.mapper

import com.misterio.core.database.entity.ChildSkillAssessmentEntity
import com.misterio.core.network.dto.AssessmentAnswerResponse
import com.misterio.core.network.dto.AssessmentResponse
import com.misterio.core.network.dto.DomainReadinessResponse
import com.misterio.core.network.dto.OptionResponse
import com.misterio.core.network.dto.PeriodReadinessResponse
import com.misterio.core.network.dto.QuestionResponse
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentMapperTest {

    @Test
    fun `AssessmentResponse toDomain correctly maps fields`() {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val qId = UUID.randomUUID()
        val oId = UUID.randomUUID()
        val ansId = UUID.randomUUID()

        val dto = AssessmentResponse(
            id = id,
            childId = childId,
            assessmentDate = "2025-01-01T12:00:00Z",
            answers = listOf(
                AssessmentAnswerResponse(
                    id = ansId,
                    question = QuestionResponse(qId, "Text", "MOTOR", 1.0, emptyList()),
                    selectedOption = OptionResponse(oId, "Opt", 1.0, 1),
                    answeredAt = "2025-01-01T12:00:00Z"
                )
            )
        )

        val domain = dto.toDomain()

        assertEquals(id, domain.id)
        assertEquals(childId, domain.childId)
        assertEquals(1, domain.answers.size)
        assertEquals(ansId, domain.answers.first().id)
        assertNull(domain.completedAt)
    }

    @Test
    fun `AssessmentResponse toEntity sets isSynced to true`() {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val periodId = UUID.randomUUID()

        val dto = AssessmentResponse(
            id = id,
            childId = childId,
            assessmentDate = "2025-01-01T12:00:00Z",
            answers = emptyList()
        )

        val entity = dto.toEntity(periodId)

        assertEquals(id, entity.id)
        assertEquals(childId, entity.childId)
        assertEquals(periodId, entity.periodId)
        assertTrue(entity.isSynced)
    }

    @Test
    fun `ChildSkillAssessmentEntity toDomain maps completedAt when present`() {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()

        val entity = ChildSkillAssessmentEntity(
            id = id,
            childId = childId,
            periodId = null,
            answers = emptyList(),
            assessmentDate = "2025-01-01T12:00:00Z",
            completedAt = "2025-01-01T13:00:00Z",
            isSynced = true
        )

        val domain = entity.toDomain()

        assertEquals(id, domain.id)
        assertEquals(childId, domain.childId)
        assertNotNull(domain.completedAt)
    }

    @Test
    fun `PeriodReadinessResponse toDomain maps readiness and domain status`() {
        val domainRes = DomainReadinessResponse(
            domain = "COGNITIVE",
            completionPercent = 80,
            isReady = true,
            requiredPercent = 75
        )
        val periodRes = PeriodReadinessResponse(
            isReady = true,
            completionPercent = 85,
            requiredPercent = 80,
            domainStatus = listOf(domainRes),
            reasons = listOf("All set"),
            canTransitionEarly = false
        )

        val domain = periodRes.toDomain()

        assertTrue(domain.isReady)
        assertEquals(85, domain.completionPercent)
        assertEquals(1, domain.domainStatus.size)
        assertEquals("COGNITIVE", domain.domainStatus.first().domain)
    }
}
