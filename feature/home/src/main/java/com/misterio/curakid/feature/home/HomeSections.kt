package com.misterio.curakid.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.LifePeriodAiReport
import com.misterio.core.ui.components.EventListItem
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun UpcomingList(
    items: List<Event>,
    onItemClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            items.take(3).forEachIndexed { index, item ->
                UpcomingListItem(event = item, onClick = { onItemClick(item) })
                if (index < items.lastIndex && index < 2) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = CurakidTheme.paddings.medium),
                        thickness = 0.5.dp,
                        color = CurakidTheme.colors.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingListItem(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EventListItem(
        event = event,
        onClick = onClick,
        dotSize = 8.dp,
        padding = CurakidTheme.paddings.medium,
        modifier = modifier
    )
}

@Composable
fun AiReportsRow(
    reports: List<LifePeriodAiReport>,
    onItemClick: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small),
        contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium)
    ) {
        items(
            items = reports,
            key = { it.id },
            contentType = { "ai_report" }
        ) { report ->
            AiReportCard(report = report, onClick = { onItemClick(report.id) })
        }
    }
}

@Composable
fun AiReportCard(
    report: LifePeriodAiReport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(260.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(CurakidTheme.paddings.medium)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CurakidTheme.colors.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = CurakidTheme.paddings.spaceXs)
            ) {
                Text(
                    text = stringResource(R.string.home_ai_report_title),
                    style = CurakidTheme.typography.labelSmall,
                    color = CurakidTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(CurakidTheme.paddings.spaceSm))
            Text(
                text = report.reportText ?: stringResource(R.string.home_ai_report_empty),
                style = CurakidTheme.typography.bodyMedium,
                color = CurakidTheme.colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
