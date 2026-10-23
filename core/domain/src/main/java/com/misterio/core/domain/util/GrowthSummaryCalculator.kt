package com.misterio.core.domain.util

import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class GrowthTrend {
    STABLE,
    INCREASING,
    DECREASING,
    UNKNOWN
}

enum class ZScoreStatus {
    NORMAL,
    ABOVE_NORMAL,
    MODERATE_DEFICIT,
    SIGNIFICANT_DEVIATION,
    UNKNOWN
}

data class GrowthSummary(
    val zScoreText: String,
    val zScoreStatus: ZScoreStatus,
    val percentileText: String,
    val trend: GrowthTrend,
    val lastMeasurement: Measurement?
)

object GrowthSummaryCalculator {

    fun calculateSummary(
        child: Child?,
        measurements: List<Measurement>,
        norms: List<ChartNormPoint>,
        valueExtractor: (Measurement) -> Double?,
        zScoreExtractor: (Measurement) -> Double?,
        percentileExtractor: (Measurement) -> Double?,
        isWeightForHeight: Boolean = false
    ): GrowthSummary {
        val sorted = measurements.sortedBy { it.date }
        val relevantMeasurements = sorted.filter { m ->
            val value = valueExtractor(m)
            if (value == null) return@filter false
            if (isWeightForHeight && m.height == null) return@filter false
            true
        }

        val last = relevantMeasurements.lastOrNull()

        var zScoreText = "—"
        var zScoreStatus = ZScoreStatus.UNKNOWN
        var percentileText = "—"

        if (child != null && last != null) {
            val zValue = zScoreExtractor(last)
            val pValue = percentileExtractor(last)

            if (zValue != null) {
                zScoreText = String.format(Locale.US, "%.2f", zValue)
                zScoreStatus = when {
                    zValue in -2.0..2.0 -> ZScoreStatus.NORMAL
                    zValue > 2.0 && zValue <= 3.0 -> ZScoreStatus.ABOVE_NORMAL
                    zValue < -2.0 && zValue >= -3.0 -> ZScoreStatus.MODERATE_DEFICIT
                    else -> ZScoreStatus.SIGNIFICANT_DEVIATION
                }
            }

            if (pValue != null) {
                percentileText = "${pValue.roundToInt()}‑й"
            } else if (norms.isNotEmpty()) {
                val x = if (isWeightForHeight) {
                    last.height ?: 0.0
                } else {
                    ChronoUnit.DAYS.between(child.birthDate, last.date.toLocalDate()).toDouble()
                }

                val value = valueExtractor(last)
                if (value != null) {
                    val nearest = norms.minByOrNull { abs(it.x - x) }
                    if (nearest != null) {
                        val pct = interpolatePercentile(value, nearest)
                        percentileText = "${pct.toInt()}‑й"
                    }
                }
            }
        }

        var trend = GrowthTrend.UNKNOWN
        if (relevantMeasurements.size >= 2) {
            val lastVal = valueExtractor(relevantMeasurements.last())
            val prevVal = valueExtractor(relevantMeasurements[relevantMeasurements.size - 2])
            if (lastVal != null && prevVal != null) {
                val delta = lastVal - prevVal
                trend = when {
                    abs(delta) < (abs(prevVal) * 0.01) -> GrowthTrend.STABLE
                    delta > 0 -> GrowthTrend.INCREASING
                    else -> GrowthTrend.DECREASING
                }
            }
        }

        return GrowthSummary(
            zScoreText = zScoreText,
            zScoreStatus = zScoreStatus,
            percentileText = percentileText,
            trend = trend,
            lastMeasurement = last
        )
    }

    private fun interpolatePercentile(value: Double, norm: ChartNormPoint): Double {
        fun interp(pLow: Double, pHigh: Double, valLow: Double, valHigh: Double): Double {
            if (valHigh == valLow) return (pLow + pHigh) / 2.0
            val t = ((value - valLow) / (valHigh - valLow)).coerceIn(0.0, 1.0)
            return pLow + t * (pHigh - pLow)
        }

        return when {
            value <= norm.p3 -> 3.0
            value <= norm.p15 -> interp(3.0, 15.0, norm.p3, norm.p15)
            value <= norm.p50 -> interp(15.0, 50.0, norm.p15, norm.p50)
            value <= norm.p85 -> interp(50.0, 85.0, norm.p50, norm.p85)
            value <= norm.p97 -> interp(85.0, 97.0, norm.p85, norm.p97)
            else -> 97.0
        }
    }
}
