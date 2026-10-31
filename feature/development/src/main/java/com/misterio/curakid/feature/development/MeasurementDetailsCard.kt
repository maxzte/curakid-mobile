package com.misterio.curakid.feature.development

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.util.AgeCalculator
import com.misterio.core.ui.DateTimeUtils
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.core.ui.toShortDate
import java.time.LocalDate
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
internal fun MeasurementDetailsCard(
    measurement: Measurement?,
    child: Child?,
    selectedChartType: GrowthChartType,
    modifier: Modifier = Modifier
) {
    if (measurement == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = CurakidTheme.Radius.lg,
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(CurakidTheme.paddings.medium)) {
            Text(
                text = stringResource(R.string.measurement_details),
                style = CurakidTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = CurakidTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(CurakidTheme.paddings.spaceMd))

            val formattedDate = measurement.date.toLocalDate().toShortDate()

            val ageValue = if (selectedChartType == GrowthChartType.WeightForHeight) {
                "${measurement.height?.roundToInt() ?: 0} см"
            } else {
                val (months, days) = AgeCalculator.calculateDetailedAge(
                    child?.effectiveBirthDate ?: LocalDate.now(),
                    measurement.date.toLocalDate()
                )
                DateTimeUtils.formatAgeDetailed(months, days)
            }

            val weightText = measurement.weight?.let { String.format(Locale.US, "%.1f кг", it) } ?: "—"
            val heightText = measurement.height?.let { String.format(Locale.US, "%.1f см", it) } ?: "—"
            val bmiText = if (measurement.weight != null && measurement.height != null) {
                String.format(Locale.US, "%.2f", measurement.weight!! / (measurement.height!! / 100.0).pow(2.0))
            } else "—"
            val headCircText = measurement.headCirc?.let { String.format(Locale.US, "%.1f см", it) } ?: "—"

            DetailRow(label = stringResource(R.string.detail_date), value = formattedDate)
            DetailRow(label = stringResource(R.string.detail_age), value = ageValue)
            DetailRow(label = stringResource(R.string.detail_weight), value = weightText)
            DetailRow(label = stringResource(R.string.detail_height), value = heightText)
            DetailRow(label = stringResource(R.string.detail_bmi), value = bmiText)
            DetailRow(label = stringResource(R.string.detail_head_circ), value = headCircText)
        }
    }
}
