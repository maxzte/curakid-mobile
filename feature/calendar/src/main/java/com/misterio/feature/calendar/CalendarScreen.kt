package com.misterio.feature.calendar

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.components.SectionHeader
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val viewModel: CalendarViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onOpenEventSheet() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            CalendarScreenContent(
                name = "",
                uiState = uiState,
                onDateSelected = { viewModel.onDateSelected(it) },
                onMonthChanged = { viewModel.onMonthChanged(it) },
                onAddEvent = { viewModel.onOpenEventSheet() }
            )

            if (uiState.isEventSheetOpen) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.onCloseEventSheet() },
                    sheetState = sheetState,
                    containerColor = CurakidTheme.colors.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    EventCreationSheet(
                        state = uiState.eventForm,
                        onStateChange = { viewModel.onEventFormChange(it) },
                        onSave = { viewModel.onSaveEvent() }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarScreenContent(
    name: String,
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (LocalDate) -> Unit,
    onAddEvent: () -> Unit
) {
    val currentMonth = remember { YearMonth.from(uiState.selectedMonth) }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek() }
    val locale = LocalConfiguration.current.locales[0]
    val coroutineScope = rememberCoroutineScope()

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleMonth }.collect { month ->
            onMonthChanged(month.yearMonth.atDay(1))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium) + rememberListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
    ) {
        item {
            Text(
                text = stringResource(R.string.calendar),
                style = CurakidTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(R.string.upcoming_events_for, name),
                style = CurakidTheme.typography.bodyLarge,
            )
        }

        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = CurakidTheme.Radius.xl,
                border = BorderStroke(0.5.dp, CurakidTheme.colors.outlineVariant),
                colors = CardDefaults.outlinedCardColors(containerColor = CurakidTheme.colors.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    CalendarHeader(
                        currentMonth = uiState.selectedMonth,
                        onPreviousMonth = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.minusMonths(1))
                            }
                        },
                        onNextMonth = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.plusMonths(1))
                            }
                        }
                    )

                    HorizontalCalendar(
                        state = state,
                        dayContent = { day ->
                            Day(
                                day = day,
                                isSelected = uiState.selectedDate == day.date,
                                hasEvents = uiState.events.any { it.date.toLocalDate() == day.date },
                                onClick = { onDateSelected(it.date) }
                            )
                        },
                        monthHeader = {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (dayOfWeek in daysOfWeek) {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                                        style = CurakidTheme.typography.labelMedium,
                                        color = CurakidTheme.colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        if (uiState.vaccinesQuantity > 0) {
            item {
                ProgressSection(
                    completed = uiState.vaccinesDone,
                    total = uiState.vaccinesQuantity,
                )
            }
        }

        item {
            SectionHeader(
                title = "UPCOMING EVENTS",
                actionLabel = "Додати",
                onAction = onAddEvent
            )
        }

        items(uiState.events) { event ->
            EventCard(event)
        }
    }
}

@Composable
fun EventCreationSheet(
    state: EventFormState,
    onStateChange: (EventFormState) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CurakidTheme.paddings.medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)
    ) {
        Text(
            text = "Створити подію",
            style = CurakidTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.title,
            onValueChange = { onStateChange(state.copy(title = it)) },
            label = { Text("Назва") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventType.entries.forEach { type ->
                FilterChip(
                    selected = state.type == type,
                    onClick = { onStateChange(state.copy(type = type)) },
                    label = { Text(type.name) }
                )
            }
        }

        OutlinedTextField(
            value = state.date.toString(),
            onValueChange = { /* Handle date change */ },
            label = { Text("Дата") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        if (state.type == EventType.DOCTOR) {
            OutlinedTextField(
                value = state.location ?: "",
                onValueChange = { onStateChange(state.copy(location = it)) },
                label = { Text("Місце проведення") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = state.description ?: "",
            onValueChange = { onStateChange(state.copy(description = it)) },
            label = { Text("Опис") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = CurakidTheme.Radius.md
        ) {
            Text("Зберегти")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProgressSection(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "progress"
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Завершено вакцинацій", style = CurakidTheme.typography.bodySmall)
            Text(
                text = "$completed / $total",
                style = CurakidTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = CurakidTheme.colors.primary,
            trackColor = CurakidTheme.colors.outlineVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun Day(day: CalendarDay, isSelected: Boolean, hasEvents: Boolean, onClick: (CalendarDay) -> Unit) {
    DayCell(
        date = day.date,
        isSelected = isSelected,
        isCurrentMonth = day.position == DayPosition.MonthDate,
        hasEvents = hasEvents,
        onDateSelected = { onClick(day) }
    )
}
