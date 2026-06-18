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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import com.misterio.core.ui.theme.CurakidTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Деталі заміру",
                style = CurakidTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = CurakidTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            val formattedDate = measurement.date.toLocalDate()?.format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
            ) ?: "—"

            val ageValue = if (selectedChartType == GrowthChartType.WeightForHeight) {
                "${measurement.height?.roundToInt() ?: 0} см"
            } else {
                val totalDays = ChronoUnit.DAYS.between(
                    child?.birthDate ?: LocalDate.now(),
                    measurement.date.toLocalDate()
                ).toInt()
                
                val months = totalDays / 30
                val remainingDays = totalDays % 30
                
                if (months > 0) {
                    "$months міс $remainingDays дн"
                } else {
                    "$remainingDays дн"
                }
            }

            val weightText = measurement.weight?.let { String.format(Locale.US, "%.1f кг", it) } ?: "—"
            val heightText = measurement.height?.let { String.format(Locale.US, "%.1f см", it) } ?: "—"
            val bmiText = if (measurement.weight != null && measurement.height != null) {
                String.format(Locale.US, "%.2f", measurement.weight!! / (measurement.height!! / 100.0).pow(2.0))
            } else "—"
            val headCircText = measurement.headCirc?.let { String.format(Locale.US, "%.1f см", it) } ?: "—"

            DetailRow(label = "Дата", value = formattedDate)
            DetailRow(label = "Вік", value = ageValue)
            DetailRow(label = "Вага", value = weightText)
            DetailRow(label = "Зріст", value = heightText)
            DetailRow(label = "ІМТ", value = bmiText)
            DetailRow(label = "Обхват голови", value = headCircText)
        }
    }
}
