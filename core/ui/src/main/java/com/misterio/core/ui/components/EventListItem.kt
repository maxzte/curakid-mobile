package com.misterio.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.core.ui.toFormatted

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
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = style.label,
            style = CurakidTheme.typography.labelSmall,
            color = style.primaryColor,
            fontWeight = FontWeight.Bold
        )
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
    val style = event.type.toStyle()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
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
                color = CurakidTheme.colors.outline
            )
        }
        EventTypeBadge(type = event.type)
    }
}
