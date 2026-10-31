package com.misterio.curakid.feature.development

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun PeriodSummaryCard(
    summary: MilestoneSummary,
    modifier: Modifier = Modifier
) {
    val paddings = CurakidTheme.paddings

    val hasIssues = summary.answeredNo > 0
    val (titleColor, icon) = if (hasIssues) Pair(
        CurakidTheme.colors.error, Icons.Default.Info
    ) else Pair(
        CurakidTheme.colors.secondary, Icons.Default.CheckCircle
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = CurakidTheme.Radius.lg,
    ) {
        Column(modifier = Modifier.padding(paddings.medium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = paddings.small)
            ) {
                Icon(icon, null, tint = titleColor, modifier = Modifier.size(20.dp))
                Text(
                    text = if (hasIssues) "Зверніть увагу" else "Чудово!",
                    style = CurakidTheme.typography.bodyMedium.copy(color = titleColor)
                )
            }

            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(paddings.small),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                SummaryStatChip(
                    count = summary.answeredYes,
                    label = "Виконує",
                    bg = CurakidTheme.colors.secondaryContainer,
                    color = CurakidTheme.colors.secondary
                )
                SummaryStatChip(
                    count = summary.answeredPartial,
                    label = "Частково",
                    bg = CurakidTheme.colors.tertiaryContainer,
                    color = CurakidTheme.colors.tertiary
                )
                SummaryStatChip(
                    count = summary.answeredNo,
                    label = "Не виконує",
                    bg = CurakidTheme.colors.errorContainer,
                    color = CurakidTheme.colors.error
                )
            }

            Text(
                text = if (hasIssues)
                    "Є кілька навичок, що ще не розвинулись. Рекомендуємо обговорити з педіатром на наступному візиті."
                else
                    "Всі навички цього періоду розвиваються відповідно до норми. Продовжуйте в тому ж дусі!",
                style = CurakidTheme.typography.bodySmall.copy(color = titleColor)
            )
        }
    }
}

@Composable
fun SummaryStatChip(
    count: Int,
    label: String,
    bg: Color,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CurakidTheme.Radius.full)
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$count",
            style = CurakidTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = label,
            style = CurakidTheme.typography.labelSmall.copy(color = color)
        )
    }
}
