package com.misterio.curakid.feature.development

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.domain.model.LifePeriod
import com.misterio.core.domain.model.LifePeriodDescription
import com.misterio.core.domain.model.PeriodReadiness
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun PeriodTimeline(
    periods: List<LifePeriod>,
    selectedId: UUID,
    onSelect: (LifePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium),
        horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small),
    ) {
        items(periods) { period ->
            PeriodItem(
                label = "${period.label} міс",
                isSelected = period.id == selectedId,
                onClick = { onSelect(period) }
            )
        }
    }
}

@Composable
fun PeriodItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barColor = if (isSelected) CuraKidColors.Blue40 else CuraKidColors.Blue90
    val labelColor = if (isSelected) CuraKidColors.Blue40 else CuraKidColors.Neutral80

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(58.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = CurakidTheme.typography.labelSmall.copy(color = labelColor),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun PeriodProgressRow(
    periodLabel: String,
    summary: MilestoneSummary,
    readiness: PeriodReadiness?,
    modifier: Modifier = Modifier
) {
    val progressValue = readiness?.completionPercent?.toFloat()?.div(100f) ?: summary.progressFraction
    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(600),
        label = "period_progress"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = periodLabel,
                style = CurakidTheme.typography.titleMedium.copy(color = CuraKidColors.Blue30)
            )
            Text(
                text = if (readiness != null) "${readiness.completionPercent}%" else "${summary.completed} / ${summary.totalQuestions} виконано",
                style = CurakidTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(CurakidTheme.paddings.extraSmall))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (readiness?.isReady == true) CuraKidColors.Teal40 else CuraKidColors.Blue40,
            trackColor = CuraKidColors.Blue90,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun PeriodDescriptionCard(
    description: LifePeriodDescription?,
    modifier: Modifier = Modifier
) {
    if (description == null) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small)
    ) {
        Text(
            text = description.title,
            style = CurakidTheme.typography.titleMedium.copy(
                color = CuraKidColors.Blue30,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = description.subtitle,
            style = CurakidTheme.typography.bodyMedium.copy(
                color = CuraKidColors.Neutral40,
                lineHeight = 20.sp
            )
        )

        Spacer(Modifier.height(4.dp))

        MilestoneItem(
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            label = "Рух",
            text = description.milestones.movement
        )
        MilestoneItem(
            icon = Icons.Default.RecordVoiceOver,
            label = "Мовлення",
            text = description.milestones.speech
        )
        MilestoneItem(
            icon = Icons.Default.Psychology,
            label = "Мислення",
            text = description.milestones.cognition
        )

        if (description.tips.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CurakidTheme.Radius.md)
                    .background(CuraKidColors.Amber90.copy(alpha = 0.5f))
                    .padding(CurakidTheme.paddings.small),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                description.tips.forEach { tip ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = CuraKidColors.Amber30,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = tip,
                            style = CurakidTheme.typography.bodySmall.copy(
                                color = CuraKidColors.Amber30,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneItem(
    icon: ImageVector,
    label: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CurakidTheme.Radius.sm)
                .background(CuraKidColors.Blue90.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CuraKidColors.Blue40,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = label,
                style = CurakidTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                )
            )
            Text(
                text = text,
                style = CurakidTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp
                )
            )
        }
    }
}
