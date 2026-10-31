package com.misterio.core.data.mapper

import com.misterio.core.database.entity.ChartNormEntity
import com.misterio.core.database.entity.MeasurementEntity
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.ChartType
import com.misterio.core.domain.model.Gender
import com.misterio.core.network.dto.MeasurementResponse
import java.time.LocalDateTime
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasurementMapperTest {

    @Test
    fun `ChartNormEntity toDomain maps percentile values correctly`() {
        val entity = ChartNormEntity(
            chartType = "WFA",
            gender = "BOY",
            x = 1.0,
            p01 = 0.1, p1 = 1.0, p3 = 3.0, p5 = 5.0, p10 = 10.0,
            p15 = 15.0, p25 = 25.0, p50 = 50.0, p75 = 75.0, p85 = 85.0,
            p90 = 90.0, p95 = 95.0, p97 = 97.0, p99 = 99.0, p999 = 99.9
        )

        val domain = entity.toDomain()

        assertEquals(1.0, domain.x, 0.001)
        assertEquals(50.0, domain.p50, 0.001)
        assertEquals(99.9, domain.p999, 0.001)
    }

    @Test
    fun `ChartNormPoint toEntity maps type and gender correctly`() {
        val point = ChartNormPoint(
            x = 2.0,
            p01 = 0.1, p1 = 1.0, p3 = 3.0, p5 = 5.0, p10 = 10.0,
            p15 = 15.0, p25 = 25.0, p50 = 50.0, p75 = 75.0, p85 = 85.0,
            p90 = 90.0, p95 = 95.0, p97 = 97.0, p99 = 99.0, p999 = 99.9
        )

        val entity = point.toEntity(ChartType.WFA, Gender.MALE)

        assertEquals("WFA", entity.chartType)
        assertEquals("MALE", entity.gender)
        assertEquals(2.0, entity.x, 0.001)
    }

    @Test
    fun `MeasurementEntity toDomain maps measurement values`() {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val date = LocalDateTime.now()
        val entity = MeasurementEntity(
            id = id,
            childId = childId,
            measurementDate = date,
            aiReportId = null,
            weightKg = 5.5,
            heightCm = 60.0,
            headCircCm = 40.0,
            wfaZscore = 0.5,
            hfaZscore = 0.2,
            wfhZscore = 0.1,
            bmiZscore = 0.3,
            hcfaZscore = 0.4,
            wfaPercentile = 50.0,
            hfaPercentile = 55.0,
            wfhPercentile = 52.0,
            bmiPercentile = 53.0,
            hcfaPercentile = 54.0,
            isSynced = true
        )

        val domain = entity.toDomain()

        assertEquals(id, domain.id)
        assertEquals(childId, domain.childId)
        assertEquals(date, domain.date)
        assertEquals(5.5, domain.weight!!, 0.001)
        assertEquals(60.0, domain.height!!, 0.001)
        assertTrue(domain.isSynced)
    }

    @Test
    fun `MeasurementResponse toEntity maps response fields and sets isSynced to true`() {
        val id = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val response = MeasurementResponse(
            id = id,
            childId = childId,
            measurementDate = "2025-01-01T12:00:00Z",
            aiReportId = null,
            weightKg = 6.0,
            heightCm = 62.0,
            headCircCm = 41.0,
            wfaZscore = 0.0,
            hfaZscore = 0.0,
            wfhZscore = 0.0,
            bmiZscore = 0.0,
            hcfaZscore = 0.0,
            wfaPercentile = 50.0,
            hfaPercentile = 50.0,
            wfhPercentile = 50.0,
            bmiPercentile = 50.0,
            hcfaPercentile = 50.0
        )

        val entity = response.toEntity()

        assertEquals(id, entity.id)
        assertEquals(childId, entity.childId)
        assertEquals(6.0, entity.weightKg!!, 0.001)
        assertEquals(62.0, entity.heightCm!!, 0.001)
        assertTrue(entity.isSynced)
    }
}
