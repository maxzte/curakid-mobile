package com.misterio.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.components.SectionHeader
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarHeader(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = CurakidTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Row {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = CuraKidColors.Blue30)
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = CuraKidColors.Blue30)
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    hasEvents: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CurakidTheme.Radius.md)
            .clickable(enabled = isCurrentMonth) { onDateSelected(date) },
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
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = CurakidTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> CurakidTheme.colors.onPrimary
                    isCurrentMonth -> CurakidTheme.colors.onSurface
                    else -> CurakidTheme.colors.outline
                }
            )
        }
        if (hasEvents && isCurrentMonth) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(CurakidTheme.colors.primary, shape = CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun EventCard(event: Event) {
    val (icon, iconColor, badgeText, badgeColor, badgeTextColor) = when (event.type) {
        EventType.VACCINE -> Quintuple(
            Icons.Default.MedicalServices,
            CuraKidColors.Blue40,
            "VACCINE",
            CuraKidColors.Blue90,
            CuraKidColors.Blue40
        )
        EventType.DOCTOR -> Quintuple(
            Icons.Default.Favorite,
            Color(0xFF9C27B0), // Purple for doctor
            "DOCTOR",
            Color(0xFFF3E5F5),
            Color(0xFF9C27B0)
        )
        EventType.USER -> Quintuple(
            Icons.Default.CalendarMonth,
            CuraKidColors.Teal40,
            "USER",
            CuraKidColors.Teal90,
            CuraKidColors.Teal40
        )
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = CurakidTheme.Radius.lg,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = badgeColor,
                shape = CurakidTheme.Radius.md,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = CurakidTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CuraKidColors.Blue30
                )
                Text(
                    text = event.date.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = CurakidTheme.typography.bodySmall,
                    color = CuraKidColors.Neutral40
                )
            }
            Surface(
                color = badgeColor,
                shape = CurakidTheme.Radius.full
            ) {
                Text(
                    text = badgeText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = CurakidTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }
        }
    }
}

data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
