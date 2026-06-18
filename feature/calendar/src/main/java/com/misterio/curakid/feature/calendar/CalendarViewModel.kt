@file:OptIn(ExperimentalCoroutinesApi::class)

package com.misterio.curakid.feature.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.UiState
import com.misterio.core.domain.asUiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.usecase.AddEventUseCase
import com.misterio.core.domain.usecase.DeleteEventUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.SyncEventsUseCase
import com.misterio.core.domain.usecase.UpdateEventUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@HiltViewModel(assistedFactory = CalendarViewModel.Factory::class)
class CalendarViewModel @AssistedInject constructor(
    @Assisted("eventId") private val eventId: UUID?,
    getChildUseCase: GetChildUseCase,
    private val syncEventsUseCase: SyncEventsUseCase,
    private val getEventsUseCase: GetEventsUseCase,
    private val addEventUseCase: AddEventUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isEventSheetOpen = MutableStateFlow(false)
    private val _isEventsExpanded = MutableStateFlow(false)
    private val _eventForm = MutableStateFlow(EventFormState())
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()
    private var eventIdHandled = false
    private val _sheetClosed = MutableSharedFlow<Unit>()
    val sheetClosed = _sheetClosed.asSharedFlow()

    private val selectedChild = getChildUseCase()
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val allEvents: StateFlow<List<Event>> = selectedChild
        .flatMapLatest { child ->
            if (child == null) {
                flowOf(emptyList())
            } else {
                getEventsUseCase(child.id).map { events ->
                    events.sortedBy { it.date }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("eventId") eventId: UUID?): CalendarViewModel
    }

    init {
        viewModelScope.launch {
            selectedChild.collectLatest { child ->
                if (child == null) {
                    Log.d(TAG, "INIT: No child selected (yet)")
                    return@collectLatest
                }
                Log.i(TAG, "Syncing events for child ${child.id}")
                runCatching {
                    syncEventsUseCase(child.id)
                }.onFailure { e ->
                    Log.e(TAG, "Sync events error", e)
                }
            }
        }

        if (eventId != null) {
            loadAndEditEvent(eventId)
        }
    }

    fun loadAndEditEvent(eventId: UUID) {
        viewModelScope.launch {
            allEvents.collectLatest { events ->
                if (eventIdHandled) return@collectLatest
                events.find { it.id == eventId }?.let { event ->
                    eventIdHandled = true
                    val eventDate = event.date.toLocalDate()
                    _selectedDate.update { eventDate }
                    _selectedMonth.update { eventDate.withDayOfMonth(1) }
                    onEditEvent(event)
                }
            }
        }
    }

    val uiState: StateFlow<UiState<CalendarUiState>> = combine(
        selectedChild,
        _selectedMonth,
        _selectedDate,
        allEvents,
        _isEventSheetOpen,
        _isEventsExpanded,
        _eventForm
    ) { args: Array<Any?> ->
        val child = args[0] as Child?
        val month = args[1] as LocalDate
        val date = args[2] as LocalDate
        val events = @Suppress("UNCHECKED_CAST") (args[3] as List<Event>)
        val isSheetOpen = args[4] as Boolean
        val isExpanded = args[5] as Boolean
        val form = args[6] as EventFormState

        if (child == null) {
            CalendarUiState()
        } else {
            val selectedDateEvents = events.filter { it.date.toLocalDate() == date }
            val vaccineEvents = events.filter { it.type == EventType.VACCINE }

            CalendarUiState(
                selectedMonth = month,
                selectedDate = date,
                childName = child.name,
                vaccinesQuantity = vaccineEvents.size,
                vaccinesDone = vaccineEvents.count { it.status == EventStatus.DONE },
                filteredEvents = selectedDateEvents,
                allEvents = events,
                isEventSheetOpen = isSheetOpen,
                eventForm = if (form.id == null && !isSheetOpen) {
                    form.copy(date = date.atStartOfDay())
                } else {
                    form
                },
                isEventsExpanded = isExpanded,
                quickPreview = getQuickPreview(date, selectedDateEvents)
            )
        }
    }.asUiState()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Initial
        )

    private fun getQuickPreview(date: LocalDate, events: List<Event>): String? {
        if (events.isEmpty()) return null

        val count = events.size
        val firstEvent = events.first().title
        val dateStr = date.format(DateTimeFormatter.ofPattern("d MMMM"))
        val eventWord = when {
            count % 10 == 1 && count % 100 != 11 -> "подія"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "події"
            else -> "подій"
        }
        return "$dateStr • $count $eventWord • $firstEvent"
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.update { date }
        _isEventsExpanded.update { false }
    }

    fun onToggleExpandEvents() {
        _isEventsExpanded.update { !it }
    }

    fun onMonthChanged(date: LocalDate) {
        _selectedMonth.update { date }
    }

    fun onOpenEventSheet() {
        _isEventSheetOpen.update { true }
        _eventForm.update { EventFormState(date = _selectedDate.value.atStartOfDay()) }
    }

    fun onEditEvent(event: Event) {
        _eventForm.update {
            EventFormState(
                id = event.id,
                title = event.title,
                type = event.type,
                date = event.date,
                location = if (event is Event.General) event.location else null,
                description = event.description,
                status = event.status
            )
        }
        _isEventSheetOpen.update { true }
    }

    fun onCloseEventSheet() {
        _isEventSheetOpen.update { false }
        eventIdHandled = true
        viewModelScope.launch {
            _sheetClosed.emit(Unit)
        }
    }

    fun onDeleteEvent() {
        val form = _eventForm.value
        val eventId = form.id ?: return

        viewModelScope.launch {
            val result = deleteEventUseCase(eventId)
            if (result.isSuccess) {
                _toastMessage.emit("Подію видалено")
                onCloseEventSheet()
            } else {
                _toastMessage.emit("Помилка при видаленні події")
            }
        }
    }

    fun onEventFormChange(form: EventFormState) {
        _eventForm.update { form }
    }

    fun onSaveEvent() {
        val child = selectedChild.value ?: return
        val form = _eventForm.value

        val originalEvent = allEvents.value.find { it.id == form.id }
        if (originalEvent != null && originalEvent.status == EventStatus.DONE && originalEvent.date != form.date) {
            viewModelScope.launch {
                _toastMessage.emit("Неможливо змінити дату виконаної події")
            }
            return
        }

        val finalStatus =
            if (form.status == EventStatus.PLANNED && form.date.isBefore(LocalDateTime.now())) {
                EventStatus.SKIPPED
            } else {
                form.status
            }

        val event = Event.General(
            id = form.id ?: UUID.randomUUID(),
            title = form.title,
            type = form.type,
            date = form.date,
            location = form.location,
            description = form.description,
            status = finalStatus,
        )

        viewModelScope.launch {
            val result = if (form.id == null) {
                addEventUseCase(child.id, event)
            } else {
                updateEventUseCase(child.id, event)
            }

            if (result.isSuccess) {
                val message =
                    if (form.id == null) "Подію успішно створено" else "Подію успішно оновлено"
                _toastMessage.emit(message)
                onCloseEventSheet()
            } else {
                val message =
                    if (form.id == null) "Помилка при створенні події" else "Помилка при оновленні події"
                _toastMessage.emit(message)
            }
        }
    }

    private companion object {
        private const val TAG = "CalendarVM"
    }
}
