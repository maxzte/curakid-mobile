package com.misterio.curakid.feature.development

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import com.misterio.core.ui.theme.CurakidTheme
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun TopSummaryRow(
    child: Child?,
    measurements: List<Measurement>,
    norms: List<ChartNormPoint>,
    selectedChartType: GrowthChartType
) {
    // Sort and filter to find the latest record that actually has data for the selected type
    val sorted = measurements.sortedBy { it.date }

    val relevantMeasurements = sorted.filter { m ->
        val value = getMeasurementValue(m, selectedChartType)
        if (value == null) return@filter false
        // For WeightForHeight we specifically need height as the X-axis
        if (selectedChartType == GrowthChartType.WeightForHeight && m.height == null) return@filter false
        true
    }

    val last = relevantMeasurements.lastOrNull()

    // Estimate percentile and z-score by nearest norm
    var percentileText = "—"
    var zScoreText = "—"
    var zScoreStatus = "Статус"
    var zScoreColor = Color.Gray

    if (child != null && last != null) {
        val zValue = getZScore(last, selectedChartType)
        val pValue = getPercentile(last, selectedChartType)

        if (zValue != null) {
            zScoreText = String.format(Locale.US, "%.2f", zValue)
            zScoreStatus = when {
                zValue >= -2.0 && zValue <= 2.0 -> "Норма"
                zValue > 2.0 && zValue <= 3.0 -> "Вище норми"
                zValue < -2.0 && zValue >= -3.0 -> "Помірний дефіцит"
                else -> "Значне відхилення"
            }
            zScoreColor = when {
                zValue >= -2.0 && zValue <= 2.0 -> Color(0xFF4CAF50)
                zValue >= -3.0 && zValue <= 3.0 -> Color(0xFFFFC107)
                else -> Color(0xFFEF5350)
            }
        }

        if (pValue != null) {
            percentileText = "${pValue.roundToInt()}‑й"
        } else if (norms.isNotEmpty()) {
            val x = if (selectedChartType == GrowthChartType.WeightForHeight) {
                last.height ?: 0.0
            } else {
                ChronoUnit.DAYS.between(child.birthDate, last.date.toLocalDate()).toDouble()
            }

            val value = getMeasurementValue(last, selectedChartType)
            if (value != null) {
                // find nearest norm point
                val nearest = norms.minByOrNull { abs(it.x - x) }
                if (nearest == null) {
                    percentileText = "—"
                } else {
                    // Simple interpolation between stored percentiles
                    fun interp(pLow: Double, pHigh: Double, valLow: Double, valHigh: Double): Double {
                        if (valHigh == valLow) return (pLow + pHigh) / 2.0
                        val t = ((value - valLow) / (valHigh - valLow)).coerceIn(0.0, 1.0)
                        return pLow + t * (pHigh - pLow)
                    }

                    val pct = when {
                        value <= nearest.p3 -> 3.0
                        value <= nearest.p15 -> interp(3.0, 15.0, nearest.p3, nearest.p15)
                        value <= nearest.p50 -> interp(15.0, 50.0, nearest.p15, nearest.p50)
                        value <= nearest.p85 -> interp(50.0, 85.0, nearest.p50, nearest.p85)
                        value <= nearest.p97 -> interp(85.0, 97.0, nearest.p85, nearest.p97)
                        else -> 97.0
                    }
                    percentileText = "${pct.toInt()}‑й"
                }
            }
        }
    }

    // Trend: compare last two relevant values
    var trendText = "—"
    var trendIcon: ImageVector? = null
    var trendIconColor = Color.Gray
    if (relevantMeasurements.size >= 2) {
        val lastVal = getMeasurementValue(relevantMeasurements.last(), selectedChartType)
        val prevVal = getMeasurementValue(relevantMeasurements[relevantMeasurements.size - 2], selectedChartType)
        if (lastVal != null && prevVal != null) {
            val delta = lastVal - prevVal
            when {
                abs(delta) < (abs(prevVal) * 0.01) -> {
                    trendText = "Стабільно"
                    trendIcon = Icons.AutoMirrored.Filled.TrendingFlat
                    trendIconColor = Color.Gray
                }

                delta > 0 -> {
                    trendText = "Зростає"
                    trendIcon = Icons.AutoMirrored.Filled.TrendingUp
                    trendIconColor = Color(0xFF4CAF50)
                }

                else -> {
                    trendText = "Знижується"
                    trendIcon = Icons.AutoMirrored.Filled.TrendingDown
                    trendIconColor = Color(0xFFEF5350)
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopStatCard(
            modifier = Modifier.weight(1f),
            title = "Останній Z‑score",
            value = zScoreText,
            subtitle = zScoreStatus,
            tint = zScoreColor
        )
        TopStatCard(
            modifier = Modifier.weight(1f),
            title = "Перцентиль",
            value = percentileText,
            subtitle = "з 100 дітей",
            tint = CurakidTheme.colors.primary
        )
        TopStatCard(
            modifier = Modifier.weight(1f),
            title = "Тренд",
            value = trendText,
            subtitle = "Порівняно",
            tint = trendIconColor,
            icon = trendIcon
        )
    }
}

private fun getZScore(m: Measurement, type: GrowthChartType): Double? = when (type) {
    GrowthChartType.WeightForAge -> m.wfaZscore
    GrowthChartType.HeightForAge -> m.hfaZscore
    GrowthChartType.WeightForHeight -> m.wfhZscore
    GrowthChartType.BMI -> m.bmiZscore
    GrowthChartType.HeadCircumference -> m.hcfaZscore
}

private fun getPercentile(m: Measurement, type: GrowthChartType): Double? = when (type) {
    GrowthChartType.WeightForAge -> m.wfaPercentile
    GrowthChartType.HeightForAge -> m.hfaPercentile
    GrowthChartType.WeightForHeight -> m.wfhPercentile
    GrowthChartType.BMI -> m.bmiPercentile
    GrowthChartType.HeadCircumference -> m.hcfaPercentile
}

@Composable
private fun TopStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    tint: Color,
    icon: ImageVector? = null
) {
    Card(
        modifier = modifier
            .height(96.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = CurakidTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = CurakidTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tint,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = CurakidTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
