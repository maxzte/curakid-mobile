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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.ui.TrackFab
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.SectionHeader
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.feature.calendar.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun CalendarScreen(eventId: String? = null) {
    val viewModel: CalendarViewModel = hiltViewModel<CalendarViewModel, CalendarViewModel.Factory> { factory ->
        factory.create(eventId)
    }
    val uiState by viewModel.uiState.collectAsState()
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
                onEditEvent = { viewModel.onEditEvent(it) },
                onToggleExpand = { viewModel.onToggleExpandEvents() },
            ) { viewModel.onOpenEventSheet() }

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
            // TODO: Error screen
        }
    }
}

@Composable
private fun CalendarScreenContent(
    childName: String,
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (LocalDate) -> Unit,
    onEditEvent: (Event) -> Unit,
    onToggleExpand: () -> Unit,
    onAddEvent: () -> Unit
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

    TrackFab(show = uiState.selectedDate >= LocalDate.now(), icon = Icons.Default.Add, onClick = onAddEvent)

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
            ScreenHeader(title = "Календар", childName = childName)
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
                                eventTypes = uiState.allEvents
                                    .filter { it.date.toLocalDate() == day.date }
                                    .map { it.type },
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
                                onClick = onEditEvent
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
    }
}

@Composable
fun EventCreationSheet(
    state: EventFormState,
    onStateChange: (EventFormState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val isEditing = state.id != null
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Якщо ми не редагуємо існуючий евент, забороняємо минулі дати
                    if (!isEditing) {
                        val todayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        return utcTimeMillis >= todayMillis
                    }
                    return true
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onStateChange(state.copy(date = selectedDate.atTime(state.date.toLocalTime())))
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.date.hour,
            initialMinute = state.date.minute
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(containerColor = CurakidTheme.colors.surface),
                shape = CurakidTheme.Radius.xl
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.select_time),
                        style = CurakidTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        TextButton(onClick = {
                            val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            onStateChange(state.copy(date = state.date.toLocalDate().atTime(selectedTime)))
                            showTimePicker = false
                        }) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CurakidTheme.paddings.medium)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) stringResource(R.string.edit_event) else stringResource(R.string.create_event),
                style = CurakidTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isEditing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.is_done),
                        style = CurakidTheme.typography.bodyMedium
                    )
                    Checkbox(
                        checked = state.status == EventStatus.DONE,
                        onCheckedChange = { isChecked ->
                            onStateChange(
                                state.copy(
                                    status = if (isChecked) EventStatus.DONE else EventStatus.PLANNED
                                )
                            )
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.title,
            onValueChange = { onStateChange(state.copy(title = it)) },
            label = { Text(stringResource(R.string.title)) },
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showDatePicker = true }
                )
        ) {
            OutlinedTextField(
                value = state.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm")),
                onValueChange = { },
                label = { Text(stringResource(R.string.date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = CurakidTheme.colors.onSurface,
                    disabledBorderColor = CurakidTheme.colors.outline,
                    disabledLabelColor = CurakidTheme.colors.onSurfaceVariant,
                    disabledPlaceholderColor = CurakidTheme.colors.onSurfaceVariant,
                )
            )
        }

        if (state.type == EventType.DOCTOR) {
            OutlinedTextField(
                value = state.location ?: "",
                onValueChange = { onStateChange(state.copy(location = it)) },
                label = { Text(stringResource(R.string.location)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = state.description ?: "",
            onValueChange = { onStateChange(state.copy(description = it)) },
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = CurakidTheme.Radius.md
        ) {
            Text(if (isEditing) stringResource(R.string.save) else stringResource(R.string.create))
        }

        if (isEditing) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = CurakidTheme.Radius.md,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CurakidTheme.colors.error)
            ) {
                Text(stringResource(R.string.delete))
            }
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
