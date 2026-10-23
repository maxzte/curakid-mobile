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
import com.misterio.core.domain.util.GrowthSummaryCalculator
import com.misterio.core.domain.util.GrowthTrend
import com.misterio.core.domain.util.ZScoreStatus
import com.misterio.core.ui.theme.CurakidTheme

@Composable
internal fun TopSummaryRow(
    child: Child?,
    measurements: List<Measurement>,
    norms: List<ChartNormPoint>,
    selectedChartType: GrowthChartType
) {
    val summary = GrowthSummaryCalculator.calculateSummary(
        child = child,
        measurements = measurements,
        norms = norms,
        valueExtractor = { getMeasurementValue(it, selectedChartType) },
        zScoreExtractor = { getZScore(it, selectedChartType) },
        percentileExtractor = { getPercentile(it, selectedChartType) },
        isWeightForHeight = selectedChartType == GrowthChartType.WeightForHeight
    )

    val zScoreStatusText = when (summary.zScoreStatus) {
        ZScoreStatus.NORMAL -> "Норма"
        ZScoreStatus.ABOVE_NORMAL -> "Вище норми"
        ZScoreStatus.MODERATE_DEFICIT -> "Помірний дефіцит"
        ZScoreStatus.SIGNIFICANT_DEVIATION -> "Значне відхилення"
        ZScoreStatus.UNKNOWN -> "Статус"
    }

    val zScoreColor = when (summary.zScoreStatus) {
        ZScoreStatus.NORMAL -> CurakidTheme.colors.tertiary
        ZScoreStatus.ABOVE_NORMAL, ZScoreStatus.MODERATE_DEFICIT -> com.misterio.core.ui.theme.CuraKidColors.Amber30
        ZScoreStatus.SIGNIFICANT_DEVIATION -> CurakidTheme.colors.error
        ZScoreStatus.UNKNOWN -> Color.Gray
    }

    val (trendText, trendIcon, trendIconColor) = when (summary.trend) {
        GrowthTrend.STABLE -> Triple("Стабільно", Icons.AutoMirrored.Filled.TrendingFlat, Color.Gray)
        GrowthTrend.INCREASING -> Triple("Зростає", Icons.AutoMirrored.Filled.TrendingUp, CurakidTheme.colors.tertiary)
        GrowthTrend.DECREASING -> Triple("Знижується", Icons.AutoMirrored.Filled.TrendingDown, CurakidTheme.colors.error)
        GrowthTrend.UNKNOWN -> Triple("—", null, Color.Gray)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopStatCard(
            modifier = Modifier.weight(1f),
            title = "Останній Z‑score",
            value = summary.zScoreText,
            subtitle = zScoreStatusText,
            tint = zScoreColor
        )
        TopStatCard(
            modifier = Modifier.weight(1f),
            title = "Перцентиль",
            value = summary.percentileText,
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
