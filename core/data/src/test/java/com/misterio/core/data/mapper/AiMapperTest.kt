package com.misterio.core.data.mapper

import com.misterio.core.database.entity.ChildReportEntity
import com.misterio.core.network.dto.ReportResponse
import java.time.LocalDateTime
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class AiMapperTest {

    @Test
    fun `ReportResponse toEntity correctly maps all fields`() {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val lifePeriodId = UUID.randomUUID()
        val triggerMeasurementId = UUID.randomUUID()
        val dto = ReportResponse(
            id = id,
            childId = childId,
            lifePeriodId = lifePeriodId,
            triggerMeasurementId = triggerMeasurementId,
            reportText = "Growth is normal",
            recommendations = "Keep feeding well",
            isFallback = false,
            urgencyScore = 1.0,
            createdAt = "2025-01-01T10:00:00Z"
        )

        val entity = dto.toEntity()

        assertEquals(id, entity.id)
        assertEquals(childId, entity.childId)
        assertEquals(lifePeriodId, entity.lifePeriodId)
        assertEquals(triggerMeasurementId, entity.triggerMeasurementId)
        assertEquals("Growth is normal", entity.reportText)
        assertEquals("Keep feeding well", entity.recommendations)
        assertEquals(false, entity.isFallback)
        assertEquals(1.0, entity.urgencyScore!!, 0.001)
    }

    @Test
    fun `ChildReportEntity toDomain correctly maps all fields`() {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val lifePeriodId = UUID.randomUUID()
        val triggerMeasurementId = UUID.randomUUID()
        val entity = ChildReportEntity(
            id = id,
            childId = childId,
            lifePeriodId = lifePeriodId,
            triggerMeasurementId = triggerMeasurementId,
            reportText = "Normal development",
            recommendations = "Daily walks",
            isFallback = true,
            urgencyScore = 2.0,
            createdAt = LocalDateTime.now()
        )

        val domain = entity.toDomain()

        assertEquals(id, domain.id)
        assertEquals(childId, domain.childId)
        assertEquals(lifePeriodId, domain.lifePeriodId)
        assertEquals(triggerMeasurementId, domain.triggerMeasurementId)
        assertEquals("Normal development", domain.reportText)
        assertEquals("Daily walks", domain.recommendations)
        assertEquals(true, domain.isFallback)
        assertEquals(2.0, domain.urgencyScore!!, 0.001)
    }
}
