package com.misterio.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun AiReportCard(
    title: String,
    reportText: String,
    recommendations: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = CurakidTheme.colors.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Хедер
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "AI Report",
                    tint = CurakidTheme.colors.primary
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = CurakidTheme.colors.outlineVariant.copy(alpha = 0.5f))

            // Тіло: Звіт
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Звіт:",
                    fontWeight = FontWeight.Bold,
                    color = CurakidTheme.colors.primary
                )
                Text(
                    text = reportText,
                    style = CurakidTheme.typography.bodyMedium,
                    color = CurakidTheme.colors.onSurfaceVariant
                )
            }

            // Тіло: Рекомендації
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Рекомендації:",
                    style = CurakidTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CurakidTheme.colors.primary
                )
                Text(
                    text = recommendations,
                    style = CurakidTheme.typography.bodyMedium,
                    color = CurakidTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}
