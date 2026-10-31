package com.misterio.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.R
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.core.ui.toFormatted
import com.misterio.core.ui.toFormattedDate

@Composable
fun EventTypeBadge(
    type: EventType,
    modifier: Modifier = Modifier
) {
    val style = type.toStyle()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(style.containerColor)
            .padding(horizontal = 10.dp, vertical = CurakidTheme.paddings.spaceXs)
    ) {
        Text(
            text = style.label,
            style = CurakidTheme.typography.labelSmall,
            color = style.primaryColor,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun EventType.toIcon(): ImageVector = when (this) {
    EventType.VACCINE -> Icons.Default.MedicalServices
    EventType.DOCTOR -> Icons.Default.Favorite
    EventType.USER -> Icons.Default.CalendarMonth
}

/**
 * Unified event item component for CuraKid.
 * Can be displayed as a compact row (for Home/Upcoming) or as an expanded card (for Calendar).
 */
@Composable
fun CuraKidEventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEditClick: (() -> Unit)? = null,
    isCardStyle: Boolean = false
) {
    val isCompleted = event.status == EventStatus.DONE || event.status == EventStatus.SKIPPED
    val contentAlpha = if (isCompleted) 0.5f else 1f
    val style = event.type.toStyle()

    if (isCardStyle) {
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .alpha(contentAlpha)
                .clickable { onClick() },
            shape = CurakidTheme.Radius.lg,
            border = if (isCompleted) BorderStroke(1.dp, CurakidTheme.colors.outlineVariant.copy(alpha = 0.5f))
            else BorderStroke(1.dp, CurakidTheme.colors.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(CurakidTheme.paddings.medium)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = style.containerColor,
                    shape = CurakidTheme.Radius.md,
                ) {
                    Icon(
                        imageVector = event.type.toIcon(),
                        contentDescription = null,
                        modifier = Modifier.padding(CurakidTheme.paddings.spaceSm),
                        tint = style.primaryColor
                    )
                }
                Spacer(modifier = Modifier.width(CurakidTheme.paddings.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = CurakidTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CurakidTheme.colors.onSurface
                    )
                    Text(
                        text = event.date.toFormattedDate(),
                        style = CurakidTheme.typography.bodySmall,
                        color = CurakidTheme.colors.onSurfaceVariant
                    )
                }
                EventTypeBadge(type = event.type)

                if (onEditClick != null) {
                    Spacer(modifier = Modifier.width(CurakidTheme.paddings.spaceSm))
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.action_edit),
                            tint = CurakidTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(CurakidTheme.paddings.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.spaceMd)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(style.primaryColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = CurakidTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = event.date.toFormatted(),
                    style = CurakidTheme.typography.bodySmall,
                    color = CurakidTheme.colors.onSurfaceVariant
                )
            }
            EventTypeBadge(type = event.type)
        }
    }
}

@Composable
fun EventListItem(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dotSize: Dp = 10.dp,
    padding: Dp = 16.dp
) {
    CuraKidEventCard(
        event = event,
        onClick = onClick,
        modifier = modifier,
        isCardStyle = false
    )
}
