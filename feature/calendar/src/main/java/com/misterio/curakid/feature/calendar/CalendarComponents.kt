package com.misterio.curakid.feature.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.components.CuraKidEventCard
import com.misterio.core.ui.components.toStyle
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.core.ui.toMonthYear
import java.time.LocalDate

@Composable
fun CalendarHeader(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentMonth.toMonthYear(),
            style = CurakidTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onTodayClick,
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(
                            width = 1.5.dp,
                            color = CurakidTheme.colors.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LocalDate.now().dayOfMonth.toString(),
                        style = CurakidTheme.typography.labelSmall,
                        color = CurakidTheme.colors.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(CurakidTheme.paddings.spaceXs))

            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.cd_previous_month),
                    tint = CurakidTheme.colors.primary
                )
            }
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.cd_next_month),
                    tint = CurakidTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    eventTypes: List<EventType>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = date == LocalDate.now()
    val scale by animateFloatAsState(if (isSelected) 1.1f else 1f, label = "scale")

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(CurakidTheme.Radius.md)
            .clickable(
                enabled = isCurrentMonth,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDateSelected(date) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = when {
                        isSelected -> CurakidTheme.colors.primary
                        else -> Color.Transparent
                    },
                    shape = CurakidTheme.Radius.md
                )
                .then(
                    if (isToday && !isSelected) {
                        Modifier.border(
                            1.dp,
                            CurakidTheme.colors.primary,
                            CurakidTheme.Radius.md
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = CurakidTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> CurakidTheme.colors.onPrimary
                    isToday -> CurakidTheme.colors.primary
                    isCurrentMonth -> CurakidTheme.colors.onSurface
                    else -> CurakidTheme.colors.outline
                }
            )
        }
        
        Row(
            modifier = Modifier
                .height(8.dp)
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCurrentMonth) {
                eventTypes.take(3).forEach { type ->
                    val color = type.toStyle().primaryColor
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(color, shape = CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickPreview(
    preview: String?,
    modifier: Modifier = Modifier
) {
    var lastPreview by remember { mutableStateOf<String?>(null) }
    if (preview != null) {
        lastPreview = preview
    }

    AnimatedVisibility(
        visible = preview != null,
        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        lastPreview?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CurakidTheme.colors.primaryContainer.copy(alpha = 0.4f),
                shape = CurakidTheme.Radius.lg
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.EventNote,
                        contentDescription = null,
                        tint = CurakidTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = it,
                        style = CurakidTheme.typography.bodyMedium,
                        color = CurakidTheme.colors.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyEventState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .alpha(0.3f),
            tint = CurakidTheme.colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_events_on_this_day),
            style = CurakidTheme.typography.bodyLarge,
            color = CurakidTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    CuraKidEventCard(
        event = event,
        onClick = onClick,
        onEditClick = onEditClick,
        isCardStyle = true
    )
}
