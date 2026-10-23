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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.domain.model.Question
import com.misterio.core.ui.theme.CuraKidColors
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
                        .background(category.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.iconTint,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = category.label,
                    style = CurakidTheme.typography.labelMedium.copy(color = CuraKidColors.Neutral40)
                )
            }

            // Question text
            Text(
                text = question.text,
                style = CurakidTheme.typography.bodyMedium.copy(
                    color = CuraKidColors.Blue30,
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
                        text = "Змінити",
                        style = CurakidTheme.typography.labelMedium.copy(color = CuraKidColors.Neutral40),
                        modifier = Modifier.clickable { onAnswerChange(null) }
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnswerOption(
                        label = "Так",
                        icon = Icons.Default.Check,
                        activeColor = CuraKidColors.Teal40,
                        activeBg = CuraKidColors.Teal90,
                        activeBorder = CuraKidColors.Teal40,
                        isSelected = abs((selectedOptionValue ?: -1.0) - 100.0) < 0.1,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(100.0) }
                    )
                    AnswerOption(
                        label = "Частково",
                        icon = Icons.Default.Remove,
                        activeColor = CuraKidColors.Amber30,
                        activeBg = CuraKidColors.Amber90,
                        activeBorder = CuraKidColors.Amber30,
                        isSelected = abs((selectedOptionValue ?: -1.0) - 50.0) < 0.1,
                        modifier = Modifier.weight(1f),
                        onClick = { onAnswerChange(50.0) }
                    )
                    AnswerOption(
                        label = "Ні",
                        icon = Icons.Default.Close,
                        activeColor = CuraKidColors.Red30,
                        activeBg = CuraKidColors.Red90,
                        activeBorder = CuraKidColors.Red30,
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
            .background(if (isSelected) activeBg else CuraKidColors.White)
            .border(
                0.5.dp,
                if (isSelected) activeBorder else CuraKidColors.Blue90,
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
            tint = if (isSelected) activeColor else CuraKidColors.Neutral40,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = CurakidTheme.typography.labelSmall.copy(
                color = if (isSelected) activeColor else CuraKidColors.Neutral40
            )
        )
    }
}

@Composable
fun AnswerBadge(optionValue: Double, modifier: Modifier = Modifier) {
    val (bg, color, icon, text) = when (optionValue) {
        100.0 -> AnswerBadgeData(
            CuraKidColors.Teal90,
            CuraKidColors.Teal40,
            Icons.Default.Check,
            "Виконує"
        )
        50.0 -> AnswerBadgeData(
            CuraKidColors.Amber90,
            CuraKidColors.Amber30,
            Icons.Default.Remove,
            "Частково"
        )
        0.0 -> AnswerBadgeData(
            CuraKidColors.Red90,
            CuraKidColors.Red30,
            Icons.Default.Close,
            "Не виконує"
        )
        else -> AnswerBadgeData(
            CuraKidColors.Blue95,
            CuraKidColors.Blue40,
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
