@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.calendar

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.TrackFab
import com.misterio.core.ui.components.ErrorView
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.SectionHeader
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.UUID

@Composable
fun CalendarScreen(
    eventId: UUID? = null,
    onVaccinationClick: (UUID) -> Unit = {},
    onChatClick: () -> Unit = {},
) {
    val viewModelKey = "$eventId"
    val viewModel: CalendarViewModel = hiltViewModel<CalendarViewModel, CalendarViewModel.Factory>(
        key = viewModelKey
    ) { factory ->
        factory.create(eventId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val sheetState = rememberModalBottomSheetState()

    when (val state = uiState) {
        is UiState.Loading, UiState.Initial -> LoadingScreen()
        is UiState.Success -> {
            val data = state.data
            CalendarScreenContent(
                childName = data.childName,
                uiState = data,
                onDateSelected = { viewModel.onDateSelected(it) },
                onMonthChanged = { viewModel.onMonthChanged(it) },
                onEventClick = { event ->
                    if (event.type == EventType.VACCINE) {
                        onVaccinationClick(event.id)
                    }
                },
                onEditEvent = { viewModel.onEditEvent(it) },
                onToggleExpand = { viewModel.onToggleExpandEvents() },
                onAddEvent = { viewModel.onOpenEventSheet() },
                onChatClick = onChatClick
            )

            if (data.isEventSheetOpen) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.onCloseEventSheet() },
                    sheetState = sheetState,
                    containerColor = CurakidTheme.colors.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    EventCreationSheet(
                        state = data.eventForm,
                        onStateChange = { viewModel.onEventFormChange(it) },
                        onSave = { viewModel.onSaveEvent() },
                        onDelete = { viewModel.onDeleteEvent() }
                    )
                }
            }
        }

        is UiState.Error -> {
            ErrorView(
                error = state.error,
                onRetry = { viewModel.refresh() }
            )
        }
    }
}

@Composable
private fun CalendarScreenContent(
    childName: String,
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (LocalDate) -> Unit,
    onEventClick: (Event) -> Unit,
    onEditEvent: (Event) -> Unit,
    onToggleExpand: () -> Unit,
    onAddEvent: () -> Unit,
    onChatClick: () -> Unit = {}
) {
    val currentMonth = remember { YearMonth.from(uiState.selectedMonth) }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek() }
    val locale = LocalConfiguration.current.locales[0]
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    TrackFab(
        show = uiState.selectedDate >= LocalDate.now(),
        icon = Icons.Default.Add,
        onClick = onAddEvent
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
            ScreenHeader(title = stringResource(R.string.calendar), childName = childName, onChatClick = onChatClick)
        }

        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = CurakidTheme.Radius.xl,
                border = BorderStroke(0.5.dp, CurakidTheme.colors.outlineVariant),
                colors = CardDefaults.outlinedCardColors(containerColor = CurakidTheme.colors.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    CalendarHeader(
                        currentMonth = uiState.selectedMonth,
                        onPreviousMonth = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(
                                    state.firstVisibleMonth.yearMonth.minusMonths(1)
                                )
                            }
                        },
                        onNextMonth = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(
                                    state.firstVisibleMonth.yearMonth.plusMonths(1)
                                )
                            }
                        },
                        onTodayClick = {
                            coroutineScope.launch {
                                onDateSelected(LocalDate.now())
                                state.animateScrollToMonth(YearMonth.now())
                            }
                        }
                    )

                    HorizontalCalendar(
                        state = state,
                        dayContent = { day ->
                            Day(
                                day = day,
                                isSelected = uiState.selectedDate == day.date,
                                eventTypes = uiState.eventTypesByDate[day.date] ?: emptyList(),
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onDateSelected(it.date)
                                }
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

        item {
            QuickPreview(preview = uiState.quickPreview)
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
            val dateStr = uiState.selectedDate.format(DateTimeFormatter.ofPattern("d MMMM"))
            SectionHeader(
                title = stringResource(R.string.events_on, dateStr).uppercase(),
                actionLabel = "",
                onAction = onAddEvent
            )
        }

        item {
            AnimatedContent(
                targetState = uiState.filteredEvents,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(300)
                    ) togetherWith fadeOut(
                        animationSpec = tween(300)
                    )
                },
                label = "eventList"
            ) { events ->
                Column(verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)) {
                    if (events.isEmpty()) {
                        EmptyEventState()
                    } else {
                        val visibleEvents = if (uiState.isEventsExpanded) events
                        else events.take(2)

                        visibleEvents.forEach { event ->
                            EventCard(
                                event = event,
                                onClick = { onEventClick(event) },
                                onEditClick = { onEditEvent(event) }
                            )
                        }

                        if (!uiState.isEventsExpanded && events.size > 2) {
                            TextButton(
                                onClick = onToggleExpand,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.more_events, events.size - 2))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ProgressSection(completed: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "progress"
    )

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.vaccination_progress),
                style = CurakidTheme.typography.bodySmall
            )
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
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    eventTypes: List<EventType>,
    onClick: (CalendarDay) -> Unit
) {
    DayCell(
        date = day.date,
        isSelected = isSelected,
        isCurrentMonth = day.position == DayPosition.MonthDate,
        eventTypes = eventTypes,
        onDateSelected = { onClick(day) }
    )
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    CurakidTheme {
        CalendarScreenContent(
            childName = "Богдан",
            uiState = CalendarUiState(
                childName = "Богдан",
                selectedDate = LocalDate.now(),
                selectedMonth = LocalDate.now(),
                filteredEvents = emptyList(),
                allEvents = emptyList()
            ),
            onDateSelected = {},
            onMonthChanged = {},
            onEventClick = {},
            onEditEvent = {},
            onToggleExpand = {},
            onAddEvent = {}
        )
    }
}
