package com.misterio.core.domain.util

import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.Measurement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class GrowthSummaryCalculatorTest {

    private val child = Child(
        id = UUID.randomUUID(),
        name = "Тест",
        birthDate = LocalDate.now().minusMonths(6),
        gender = Gender.MALE,
        isPremature = false,
        gestationalWeeks = null,
        allergies = emptyList(),
        chronicDiseases = emptyList(),
        pastDiseases = emptyList(),
        specialFeatures = emptyList()
    )

    private fun createNormPoint(x: Double, p3: Double, p15: Double, p50: Double, p85: Double, p97: Double) =
        ChartNormPoint(
            x = x,
            p01 = p3 * 0.8,
            p1 = p3 * 0.9,
            p3 = p3,
            p5 = p3 * 1.05,
            p10 = p15 * 0.95,
            p15 = p15,
            p25 = (p15 + p50) / 2,
            p50 = p50,
            p75 = (p50 + p85) / 2,
            p85 = p85,
            p90 = p85 * 1.05,
            p95 = p97 * 0.95,
            p97 = p97,
            p99 = p97 * 1.1,
            p999 = p97 * 1.2
        )

    @Test
    fun calculateSummary_withZscoreAndPercentile_returnsCorrectValues() {
        val measurement = Measurement(
            id = UUID.randomUUID(),
            childId = child.id,
            date = LocalDateTime.now(),
            weight = 7.5,
            height = 68.0,
            headCirc = 43.0,
            wfaZscore = 0.5,
            wfaPercentile = 69.1,
            hfaZscore = null,
            hfaPercentile = null,
            wfhZscore = null,
            wfhPercentile = null,
            bmiZscore = null,
            bmiPercentile = null,
            hcfaZscore = null,
            hcfaPercentile = null,
            aiReportId = null
        )

        val summary = GrowthSummaryCalculator.calculateSummary(
            child = child,
            measurements = listOf(measurement),
            norms = emptyList(),
            valueExtractor = { it.weight },
            zScoreExtractor = { it.wfaZscore },
            percentileExtractor = { it.wfaPercentile }
        )

        assertEquals("0.50", summary.zScoreText)
        assertEquals(ZScoreStatus.NORMAL, summary.zScoreStatus)
        assertEquals("69‑й", summary.percentileText)
        assertEquals(GrowthTrend.UNKNOWN, summary.trend)
        assertNotNull(summary.lastMeasurement)
    }

    @Test
    fun calculateSummary_trendIncreasing_detectedCorrectly() {
        val m1 = Measurement(
            id = UUID.randomUUID(),
            childId = child.id,
            date = LocalDateTime.now().minusDays(30),
            weight = 7.0,
            height = null,
            headCirc = null,
            wfaZscore = 0.0,
            wfaPercentile = 50.0,
            hfaZscore = null,
            hfaPercentile = null,
            wfhZscore = null,
            wfhPercentile = null,
            bmiZscore = null,
            bmiPercentile = null,
            hcfaZscore = null,
            hcfaPercentile = null,
            aiReportId = null
        )
        val m2 = m1.copy(
            id = UUID.randomUUID(),
            date = LocalDateTime.now(),
            weight = 7.5,
            wfaZscore = 0.5,
            wfaPercentile = 69.0
        )

        val summary = GrowthSummaryCalculator.calculateSummary(
            child = child,
            measurements = listOf(m1, m2),
            norms = emptyList(),
            valueExtractor = { it.weight },
            zScoreExtractor = { it.wfaZscore },
            percentileExtractor = { it.wfaPercentile }
        )

        assertEquals(GrowthTrend.INCREASING, summary.trend)
    }

    @Test
    fun calculateSummary_interpolatesPercentileFromNormsWhenMissing() {
        val measurement = Measurement(
            id = UUID.randomUUID(),
            childId = child.id,
            date = LocalDateTime.now(),
            weight = 8.0,
            height = null,
            headCirc = null,
            wfaZscore = null,
            wfaPercentile = null,
            hfaZscore = null,
            hfaPercentile = null,
            wfhZscore = null,
            wfhPercentile = null,
            bmiZscore = null,
            bmiPercentile = null,
            hcfaZscore = null,
            hcfaPercentile = null,
            aiReportId = null
        )

        val days = java.time.temporal.ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble()
        val norm = createNormPoint(
            x = days,
            p3 = 6.0,
            p15 = 7.0,
            p50 = 8.0,
            p85 = 9.0,
            p97 = 10.0
        )

        val summary = GrowthSummaryCalculator.calculateSummary(
            child = child,
            measurements = listOf(measurement),
            norms = listOf(norm),
            valueExtractor = { it.weight },
            zScoreExtractor = { it.wfaZscore },
            percentileExtractor = { it.wfaPercentile }
        )

        assertEquals("50‑й", summary.percentileText)
    }
}
