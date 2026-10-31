package com.misterio.curakid.feature.development

import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import java.time.temporal.ChronoUnit
import java.util.Locale

data class GrowthChartPeriod(val start: Double, val end: Double)

data class GrowthChartComputationResult(
    val filteredNorms: List<ChartNormPoint>,
    val filteredChildPoints: List<Pair<Double, Double>>,
    val minY: Double,
    val maxY: Double
)

object GrowthChartCalculator {

    fun roundForChart(value: Double): Double =
        String.format(Locale.US, "%.3f", value).toDouble()

    fun parsePeriod(label: String, lifePeriods: List<String>): GrowthChartPeriod {
        return if (label.contains("-")) {
            val parts = label.split("-")
            val start = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
            val end = parts.getOrNull(1)?.toDoubleOrNull() ?: 72.0
            GrowthChartPeriod(start, end)
        } else {
            val end = label.toDoubleOrNull() ?: 72.0
            val idx = lifePeriods.indexOf(label)
            val start = if (idx > 0) {
                val prevLabel = lifePeriods[idx - 1]
                if (prevLabel.contains("-")) prevLabel.split("-").last().toDoubleOrNull() ?: 0.0
                else prevLabel.toDoubleOrNull() ?: 0.0
            } else 0.0
            GrowthChartPeriod(start, end)
        }
    }

    fun getXScale(isWfh: Boolean, period: GrowthChartPeriod): Double {
        return when {
            isWfh -> 1.0
            (period.end - period.start) <= 3.0 -> 1.0 // Days
            (period.end - period.start) <= 6.0 -> 7.0 // Weeks
            else -> 30.44 // Months
        }
    }

    fun computeChartData(
        child: Child,
        chartData: List<Measurement>,
        norms: List<ChartNormPoint>,
        chartType: GrowthChartType,
        autoPeriodLabel: String,
        lifePeriods: List<String>
    ): GrowthChartComputationResult {
        val isWfh = chartType == GrowthChartType.WeightForHeight
        val period = parsePeriod(autoPeriodLabel, lifePeriods)
        val xUnitScale = getXScale(isWfh, period)

        val filteredChildPoints = chartData.asSequence().filter { m ->
            when (chartType) {
                GrowthChartType.WeightForAge -> m.weight != null
                GrowthChartType.HeightForAge -> m.height != null
                GrowthChartType.WeightForHeight -> m.weight != null && m.height != null
                GrowthChartType.BMI -> m.weight != null && m.height != null
                GrowthChartType.HeadCircumference -> m.headCirc != null
            }
        }.map { m ->
            val y = when (chartType) {
                GrowthChartType.WeightForAge -> m.weight!!
                GrowthChartType.HeightForAge -> m.height!!
                GrowthChartType.WeightForHeight -> m.weight!!
                GrowthChartType.BMI -> {
                    val hM = m.height!! / 100.0
                    m.weight!! / (hM * hM)
                }
                GrowthChartType.HeadCircumference -> m.headCirc!!
            }

            val x = if (isWfh) {
                m.height!!
            } else {
                val days = ChronoUnit.DAYS.between(child.effectiveBirthDate, m.date.toLocalDate()).toDouble()
                (days - (period.start * 30.44)) / xUnitScale
            }
            roundForChart(x) to y
        }.toList()

        val filteredNorms = if (norms.isNotEmpty()) {
            val minXLimit = if (isWfh) 45.0 else period.start * 30.44
            val maxXLimit = if (isWfh) 120.0 else period.end * 30.44

            norms.filter { it.x in minXLimit..maxXLimit }
                .map { norm ->
                    val scaledX = if (isWfh) norm.x else (norm.x - (period.start * 30.44)) / xUnitScale
                    norm.copy(x = roundForChart(scaledX))
                }
        } else emptyList()

        val minChildY = filteredChildPoints.minOfOrNull { it.second } ?: Double.MAX_VALUE
        val maxChildY = filteredChildPoints.maxOfOrNull { it.second } ?: Double.MIN_VALUE

        val minNormY = filteredNorms.minOfOrNull { it.p3 } ?: Double.MAX_VALUE
        val maxNormY = filteredNorms.maxOfOrNull { it.p97 } ?: Double.MIN_VALUE

        val minY = minOf(minChildY, minNormY).takeIf { it != Double.MAX_VALUE } ?: 0.0
        val maxY = maxOf(maxChildY, maxNormY).takeIf { it != Double.MIN_VALUE } ?: 100.0

        return GrowthChartComputationResult(
            filteredNorms = filteredNorms,
            filteredChildPoints = filteredChildPoints,
            minY = minY,
            maxY = maxY
        )
    }
}
