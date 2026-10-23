package com.misterio.curakid.feature.development

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.components.RulerSlider
import com.misterio.core.ui.theme.CurakidTheme
import java.util.Locale
import kotlin.math.round

@Composable
fun MeasurementInputSection(
    title: String,
    unit: String,
    wholeValue: Double,
    onWholeChange: (Double) -> Unit,
    decimalValue: Double,
    onDecimalChange: (Double) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    normRange: ClosedFloatingPointRange<Float>? = null,
    modifier: Modifier = Modifier
) {
    val totalValue = wholeValue + decimalValue
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = String.format(Locale.US, "%.1f %s", totalValue, unit),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = CurakidTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        )

        NormStatus(value = totalValue.toFloat(), normRange = normRange)

        Spacer(modifier = Modifier.height(8.dp))

        RulerSlider(
            value = wholeValue.toFloat(),
            onValueChange = { onWholeChange(round(it.toDouble())) },
            range = range,
            step = 1f,
            modifier = Modifier.height(80.dp),
            normRange = normRange
        )

        RulerSlider(
            value = decimalValue.toFloat(),
            onValueChange = { onDecimalChange(it.toDouble()) },
            range = 0f..1f,
            step = 0.1f,
            modifier = Modifier.height(80.dp)
        )
    }
}
