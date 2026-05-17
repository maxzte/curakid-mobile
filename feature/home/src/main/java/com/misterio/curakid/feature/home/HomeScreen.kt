package com.misterio.curakid.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.VaccineEvent
import com.misterio.core.domain.model.VaccineType
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.SectionHeader
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.core.ui.toFormatted
import kotlinx.coroutines.launch


data class Exercise(
    val id: Int,
    val name: String,
    val type: String,
    val durationMin: Int,
    val daysLeft: Int,
    val doneCount: Int,
    val totalCount: Int,
    val emoji: String
)

data class Tip(
    val id: Int,
    val categoryLabel: String,
    val bg: Color,
    val textColor: Color,
    val text: String
)

fun sampleExercises() = listOf(
    Exercise(1, "Біг підтюпцем", "Кардіо", 20, 3, 4, 7, "🏃"),
    Exercise(2, "Розтяжка", "Гнучкість", 15, 5, 2, 7, "🧘"),
    Exercise(3, "Планка", "Сила", 10, 1, 6, 7, "💪")
)

@Composable
fun sampleTips() = listOf(
    Tip(
        1,
        "Харчування",
        CuraKidColors.Amber90,
        CuraKidColors.Amber30,
        "Додай більше заліза у раціон — зараз активний ріст потребує підтримки"
    ),
    Tip(
        2,
        "Сон",
        CuraKidColors.Purple90,
        CuraKidColors.Purple30,
        "У цьому віці рекомендовано 9–11 годин сну для кращого розвитку"
    ),
    Tip(
        3,
        "Активність",
        CuraKidColors.Teal90,
        CuraKidColors.Teal30,
        "Прогулянки на свіжому повітрі зміцнюють імунітет перед осіннім сезоном"
    )
)

@Composable
fun HomeScreen(
    onEventClick: (eventId: String) -> Unit,
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UiState.Loading, UiState.Initial -> LoadingScreen()
        is UiState.Success -> {
            val data = state.data

            HomeContent(
                childName = data.selectedChild?.name ?: "",
                nextVaccination = data.vaccineEvent,
                nextDoctorVisit = data.doctorEvent,
                upcomingList = data.upcomingItems,
                onEventClick = onEventClick,
            )
        }

        is UiState.Error -> {
            // TODO: Show error screen
        }
    }
}

@Composable
private fun HomeContent(
    childName: String = "AM",
    nextVaccination: VaccineEvent? = null,
    nextDoctorVisit: Event? = null,
    exercises: List<Exercise> = sampleExercises(),
    tips: List<Tip> = sampleTips(),
    upcomingList: List<Event> = emptyList(),
    onItemClick: (Event) -> Unit = {},
    onEventClick: (eventId: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
        contentPadding = rememberListContentPadding()
    ) {
        itemWrapper {
            ScreenHeader(title = "CuraKid", childName = childName)
        }

        item {
            HeroCardsPager(
                vaccination = nextVaccination,
                doctorVisit = nextDoctorVisit,
                onVaccinationClick = { onEventClick(it) },
                onDoctorVisitClick = { onEventClick(it) }
            )
        }

        if (upcomingList.isNotEmpty()) {
            itemWrapper {
                SectionHeader(title = "Майбутні", actionLabel = "Всі", onAction = {})
            }

            itemWrapper {
                UpcomingList(
                    items = upcomingList,
                    onItemClick = onItemClick
                )
            }
        }

        itemWrapper {
            SectionHeader(
                title = "Вправи на тиждень",
                actionLabel = "Всі →",
                onAction = {}
            )
        }

        item {
            ExercisesRow(
                exercises = exercises,
                onItemClick = {}
            )
        }

        itemWrapper {
            SectionHeader(title = "Поради для тебе", actionLabel = "", onAction = {})
        }

        item {
            TipsRow(tips = tips)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    CurakidTheme {
        HomeContent("", null, null, sampleExercises(), sampleTips(), emptyList(), {}, {})
    }
}

@Composable
private fun HeroCardsPager(
    vaccination: VaccineEvent?,
    doctorVisit: Event?,
    onVaccinationClick: (String) -> Unit,
    onDoctorVisitClick: (String) -> Unit
) {
    val items = listOfNotNull(vaccination, doctorVisit)
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium),
            pageSpacing = CurakidTheme.paddings.small,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> {
                    if (vaccination != null) {
                        VaccinationHeroCard(
                            vaccination = vaccination,
                            onClick = { onVaccinationClick(vaccination.id) },
                        )
                    }
                }

                1 -> {
                    if (doctorVisit != null) {
                        DoctorVisitHeroCard(
                            visit = doctorVisit,
                            onClick = { onDoctorVisitClick(doctorVisit.id) },
                        )
                    }
                }
            }
        }

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

@Composable
private fun VaccinationHeroCard(vaccination: VaccineEvent, onClick: () -> Unit) {
    HeroCard(onClick = onClick) {
        HeroTopRow(
            iconBg = CurakidTheme.colors.primaryContainer,
            icon = {
                Icon(
                    Icons.Default.Vaccines,
                    null,
                    tint = CurakidTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            },
            label = "Наступна вакцинація",
            badgeBg = CurakidTheme.colors.surfaceVariant,
            badgeText = if (vaccination.type == VaccineType.VACCINATION) {
                "${vaccination.doseNumber}/${vaccination.totalDoses}"
            } else "Ревакцинація",
            badgeTextColor = CurakidTheme.colors.primary
        )
        Spacer(Modifier.height(12.dp))
        HeroBottomRow(
            chevronBg = CurakidTheme.colors.primaryContainer,
            chevronTint = CurakidTheme.colors.primary
        ) {
            Text(vaccination.name, style = CurakidTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    null,
                    tint = CurakidTheme.colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    vaccination.date.toFormatted(),
                    style = CurakidTheme.typography.bodySmall,
                    color = CurakidTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun DoctorVisitHeroCard(visit: Event, onClick: () -> Unit) {
    HeroCard(onClick = onClick) {
        HeroTopRow(
            iconBg = CurakidTheme.colors.secondaryContainer,
            icon = {
                Icon(
                    Icons.Default.PersonSearch,
                    null,
                    tint = CurakidTheme.colors.secondary,
                    modifier = Modifier.size(16.dp)
                )
            },
            label = "Похід до лікаря",
            badgeBg = CuraKidColors.Teal80.copy(alpha = 0.3f),
            badgeText = "Заплановано",
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
                    Icons.Default.CalendarToday,
                    null,
                    tint = CurakidTheme.colors.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    visit.date.toFormatted(),
                    style = CurakidTheme.typography.bodySmall,
                    color = CurakidTheme.colors.secondary
                )
            }
            if (visit.location != null) {
                Text(
                    "${visit.location}",
                    style = CurakidTheme.typography.labelSmall,
                    color = CurakidTheme.colors.secondary
                )
            }
        }
    }
}

@Composable
private fun HeroCard(onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 150.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun HeroTopRow(
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
            label,
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
                badgeText,
                style = CurakidTheme.typography.labelSmall,
                color = badgeTextColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeroBottomRow(
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
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = chevronTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UpcomingList(items: List<Event>, onItemClick: (Event) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = CurakidTheme.colors.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingListItem(event: Event, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val dotColor = when (event.type) {
            EventType.VACCINE -> CurakidTheme.colors.primary
            EventType.DOCTOR -> CurakidTheme.colors.secondary
            EventType.USER -> CurakidTheme.colors.tertiary
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = event.title, style = CurakidTheme.typography.bodyMedium)

            Text(
                text = event.date.toFormatted(),
                style = CurakidTheme.typography.bodySmall
            )
        }
        UpcomingBadge(event = event)
    }
}

@Composable
private fun UpcomingBadge(event: Event) {
    val (bg, textColor, text) = when (event.type) {
        EventType.VACCINE -> Triple(
            CurakidTheme.colors.primaryContainer,
            CurakidTheme.colors.primary,
            "Вакцина"
        )

        EventType.DOCTOR -> Triple(
            CurakidTheme.colors.secondaryContainer,
            CurakidTheme.colors.secondary,
            "Лікар"
        )

        EventType.USER -> Triple(
            CurakidTheme.colors.tertiaryContainer,
            CurakidTheme.colors.tertiary,
            "Подія"
        )
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = CurakidTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExercisesRow(exercises: List<Exercise>, onItemClick: (Exercise) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small),
        contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium)
    ) {
        items(exercises) { ex ->
            ExerciseCard(exercise = ex, onClick = { onItemClick(ex) })
        }
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise, onClick: () -> Unit) {
    val progress = exercise.doneCount.toFloat() / exercise.totalCount
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "ex_progress"
    )

    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CurakidTheme.colors.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(exercise.emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(exercise.name, style = CurakidTheme.typography.bodyMedium)
            Text(
                "${exercise.type} · ${exercise.durationMin} хв",
                style = CurakidTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "${exercise.daysLeft} дн ще",
                    style = CurakidTheme.typography.labelSmall,
                    color = CurakidTheme.colors.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CurakidTheme.colors.tertiary,
                    trackColor = CurakidTheme.colors.outlineVariant,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    "${exercise.doneCount}/${exercise.totalCount}",
                    style = CurakidTheme.typography.labelSmall,
                    color = CurakidTheme.colors.tertiary
                )
            }
        }
    }
}

@Composable
private fun TipsRow(tips: List<Tip>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.small),
        contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium)
    ) {
        items(tips) { tip ->
            TipCard(tip = tip)
        }
    }
}

@Composable
private fun TipCard(tip: Tip) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.surface),
        border = BorderStroke(0.5.dp, CurakidTheme.colors.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(tip.bg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    tip.categoryLabel,
                    style = CurakidTheme.typography.labelSmall,
                    color = tip.textColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                tip.text,
                style = CurakidTheme.typography.labelMedium,
                color = CurakidTheme.colors.onSurface
            )
        }
    }
}
