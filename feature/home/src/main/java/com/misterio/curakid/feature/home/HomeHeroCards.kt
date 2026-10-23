package com.misterio.curakid.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.VaccineType
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.core.ui.toFormatted
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun HeroCardsPager(
    vaccination: Event.Vaccine?,
    doctorVisit: Event?,
    onVaccinationClick: (UUID) -> Unit,
    onDoctorVisitClick: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOfNotNull(vaccination, doctorVisit)
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium),
            pageSpacing = CurakidTheme.paddings.small,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (val item = items.getOrNull(page)) {
                is Event.Vaccine -> {
                    VaccinationHeroCard(
                        vaccination = item,
                        onClick = { onVaccinationClick(item.id) }
                    )
                }
                is Event -> {
                    DoctorVisitHeroCard(
                        visit = item,
                        onClick = { onDoctorVisitClick(item.id) }
                    )
                }
                null -> Unit
            }
        }

        if (pagerState.pageCount > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) CurakidTheme.colors.primary else CurakidTheme.colors.outlineVariant)
                            .size(width = if (isSelected) 16.dp else 6.dp, height = 6.dp)
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                    )
                }
            }
        }
    }
}

@Composable
fun VaccinationHeroCard(
    vaccination: Event.Vaccine,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HeroCard(onClick = onClick, modifier = modifier) {
        HeroTopRow(
            iconBg = CurakidTheme.colors.primaryContainer,
            icon = {
                Icon(
                    imageVector = Icons.Default.Vaccines,
                    contentDescription = null,
                    tint = CurakidTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            },
            label = stringResource(R.string.next_vaccination),
            badgeBg = CurakidTheme.colors.surfaceVariant,
            badgeText = if (vaccination.vaccineType == VaccineType.VACCINATION) {
                "${vaccination.doseNumber}/${vaccination.totalDoses}"
            } else {
                stringResource(R.string.home_revaccination)
            },
            badgeTextColor = CurakidTheme.colors.primary
        )
        Spacer(Modifier.height(12.dp))
        HeroBottomRow(
            chevronBg = CurakidTheme.colors.primaryContainer,
            chevronTint = CurakidTheme.colors.primary
        ) {
            Text(vaccination.title, style = CurakidTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = CurakidTheme.colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = vaccination.date.toFormatted(),
                    style = CurakidTheme.typography.bodySmall,
                    color = CurakidTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun DoctorVisitHeroCard(
    visit: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HeroCard(onClick = onClick, modifier = modifier) {
        HeroTopRow(
            iconBg = CurakidTheme.colors.secondaryContainer,
            icon = {
                Icon(
                    imageVector = Icons.Default.PersonSearch,
                    contentDescription = null,
                    tint = CurakidTheme.colors.secondary,
                    modifier = Modifier.size(16.dp)
                )
            },
            label = stringResource(R.string.home_doctor_visit),
            badgeBg = CuraKidColors.Teal80.copy(alpha = 0.3f),
            badgeText = stringResource(R.string.home_status_planned),
            badgeTextColor = CurakidTheme.colors.onSecondaryContainer
        )
        Spacer(Modifier.height(12.dp))
        HeroBottomRow(
            chevronBg = CurakidTheme.colors.secondaryContainer,
            chevronTint = CurakidTheme.colors.secondary
        ) {
            Text(
                text = visit.title,
                style = CurakidTheme.typography.headlineMedium.copy(color = CurakidTheme.colors.onSecondaryContainer)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = CurakidTheme.colors.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = visit.date.toFormatted(),
                    style = CurakidTheme.typography.bodySmall,
                    color = CurakidTheme.colors.secondary
                )
            }
            if (visit is Event.General && visit.location != null) {
                Text(
                    text = visit.location.orEmpty(),
                    style = CurakidTheme.typography.labelSmall,
                    color = CurakidTheme.colors.secondary
                )
            }
        }
    }
}

@Composable
fun HeroCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 150.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun HeroTopRow(
    iconBg: Color,
    icon: @Composable () -> Unit,
    label: String,
    badgeBg: Color,
    badgeText: String,
    badgeTextColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) { icon() }
        Text(
            text = label,
            style = CurakidTheme.typography.bodySmall,
            color = CurakidTheme.colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(badgeBg)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                style = CurakidTheme.typography.labelSmall,
                color = badgeTextColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HeroBottomRow(
    chevronBg: Color,
    chevronTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), content = content)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(chevronBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = chevronTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
