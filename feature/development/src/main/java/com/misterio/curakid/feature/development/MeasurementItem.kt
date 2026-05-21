package com.misterio.curakid.feature.development

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.domain.model.Measurement
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.feature.development.R
import java.time.format.DateTimeFormatter

@Composable
fun MeasurementItem(measurement: Measurement) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.padding(horizontal = CurakidTheme.paddings.medium).fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = measurement.date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (measurement.weight != null) {
                        MeasurementValue(
                            value = "%.1f kg".format(measurement.weight),
                            label = stringResource(R.string.weight)
                        )
                    }
                    if (measurement.height != null) {
                        MeasurementValue(
                            value = "%.1f cm".format(measurement.height),
                            label = stringResource(R.string.height)
                        )
                    }
                    if (measurement.headCirc != null) {
                        MeasurementValue(
                            value = "%.1f cm".format(measurement.headCirc),
                            label = stringResource(R.string.head_circumference)
                        )
                    }
                }
            }

            if (!measurement.isSynced) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Not synced",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun MeasurementValue(value: String, label: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp
            )
        )
    }
}
