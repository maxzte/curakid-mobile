package com.misterio.curakid.feature.development

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.domain.model.Question
import com.misterio.core.ui.theme.CurakidTheme
import kotlin.math.abs

@Composable
fun QuizCard(
    question: Question,
    selectedOptionValue: Double?,
    onAnswerChange: (Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    val paddings = CurakidTheme.paddings
    val category = MilestoneCategory.fromDomain(question.domain)

    OutlinedCard(
        modifier = modifier
            .padding(horizontal = paddings.medium)
            .fillMaxWidth(),
        shape = CurakidTheme.Radius.lg,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {

            // Category row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = paddings.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CurakidTheme.Radius.sm)
                        .background(category.getIconBg()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.getIconTint(),
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = category.label,
                    style = CurakidTheme.typography.labelMedium.copy(color = CurakidTheme.colors.onSurfaceVariant)
                )
            }

            // Question text
            Text(
                text = question.text,
                style = CurakidTheme.typography.bodyMedium.copy(
                    color = CurakidTheme.colors.onSurface,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Answer row — if already answered show badge + edit
            if (selectedOptionValue != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnswerBadge(optionValue = selectedOptionValue)
                    Text(
                        text = stringResource(R.string.quiz_change),
                        style = CurakidTheme.typography.labelMedium.copy(color = CurakidTheme.colors.onSurfaceVariant),
                        modifier = Modifier.clickable { onAnswerChange(null) }
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.spaceSm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnswerOption(
                        label = stringResource(R.string.answer_yes),
                        icon = Icons.Default.Check,
                        activeColor = CurakidTheme.colors.secondary,
                        activeBg = CurakidTheme.colors.secondaryContainer,
                        activeBorder = CurakidTheme.colors.secondary,
                        isSelected = abs((selectedOptionValue ?: -1.0) - 100.0) < 0.1,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(100.0) }
                    )
                    AnswerOption(
                        label = stringResource(R.string.answer_partially),
                        icon = Icons.Default.Remove,
                        activeColor = CurakidTheme.colors.tertiary,
                        activeBg = CurakidTheme.colors.tertiaryContainer,
                        activeBorder = CurakidTheme.colors.tertiary,
                        isSelected = abs((selectedOptionValue ?: -1.0) - 50.0) < 0.1,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(50.0) }
                    )
                    AnswerOption(
                        label = stringResource(R.string.answer_no),
                        icon = Icons.Default.Close,
                        activeColor = CurakidTheme.colors.error,
                        activeBg = CurakidTheme.colors.errorContainer,
                        activeBorder = CurakidTheme.colors.error,
                        isSelected = abs((selectedOptionValue ?: -1.0) - 0.0) < 0.1,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(0.0) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerOption(
    label: String,
    icon: ImageVector,
    activeColor: Color,
    activeBg: Color,
    activeBorder: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(CurakidTheme.Radius.md)
            .background(if (isSelected) activeBg else CurakidTheme.colors.surfaceContainer)
            .border(
                0.5.dp,
                if (isSelected) activeBorder else CurakidTheme.colors.outline,
                CurakidTheme.Radius.md
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else CurakidTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = CurakidTheme.typography.labelSmall.copy(
                color = if (isSelected) activeColor else CurakidTheme.colors.onSurfaceVariant
            )
        )
    }
}

@Composable
fun AnswerBadge(optionValue: Double, modifier: Modifier = Modifier) {
    val (bg, color, icon, text) = when (optionValue) {
        100.0 -> AnswerBadgeData(
            CurakidTheme.colors.secondaryContainer,
            CurakidTheme.colors.secondary,
            Icons.Default.Check,
            "Виконує"
        )
        50.0 -> AnswerBadgeData(
            CurakidTheme.colors.tertiaryContainer,
            CurakidTheme.colors.tertiary,
            Icons.Default.Remove,
            "Частково"
        )
        0.0 -> AnswerBadgeData(
            CurakidTheme.colors.errorContainer,
            CurakidTheme.colors.error,
            Icons.Default.Close,
            "Не виконує"
        )
        else -> AnswerBadgeData(
            CurakidTheme.colors.primaryContainer,
            CurakidTheme.colors.primary,
            Icons.AutoMirrored.Filled.HelpOutline,
            ""
        )
    }
    Row(
        modifier = modifier
            .clip(CurakidTheme.Radius.full)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
        Text(text, style = CurakidTheme.typography.labelSmall.copy(color = color))
    }
}

private data class AnswerBadgeData(
    val bg: Color,
    val color: Color,
    val icon: ImageVector,
    val text: String
)
