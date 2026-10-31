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
import com.misterio.core.ui.util.UiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.misterio.core.domain.usecase.GetEventQuickPreviewUseCase
import java.time.LocalDateTime
import com.misterio.core.ui.toDayMonth
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
    private val getEventQuickPreviewUseCase: GetEventQuickPreviewUseCase,
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isEventSheetOpen = MutableStateFlow(false)
    private val _isEventsExpanded = MutableStateFlow(false)
    private val _eventForm = MutableStateFlow(EventFormState())
    private val _toastMessage = Channel<UiText>(Channel.BUFFERED)
    val toastMessage = _toastMessage.receiveAsFlow()
    private var eventIdHandled = false
    private val _sheetClosed = Channel<Unit>(Channel.BUFFERED)
    val sheetClosed = _sheetClosed.receiveAsFlow()

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
                syncEventsUseCase(child.id).onFailure { e ->
                    Log.e(TAG, "Sync events error", e)
                }
            }
        }

        if (eventId != null) {
            loadAndEditEvent(eventId)
        }
    }

    fun refresh() {
        val child = selectedChild.value ?: return
        viewModelScope.launch {
            syncEventsUseCase(child.id).onFailure { e ->
                Log.e(TAG, "Refresh events error", e)
            }
        }
    }

    fun loadAndEditEvent(eventId: UUID) {
        viewModelScope.launch {
            if (eventIdHandled) return@launch
            val event = allEvents.mapNotNull { events -> events.find { it.id == eventId } }.first()
            if (!eventIdHandled) {
                eventIdHandled = true
                val eventDate = event.date.toLocalDate()
                _selectedDate.update { eventDate }
                _selectedMonth.update { eventDate.withDayOfMonth(1) }
                onEditEvent(event)
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
            val eventTypesByDate = events.groupBy { it.date.toLocalDate() }
                .mapValues { entry -> entry.value.map { it.type }.distinct() }

            CalendarUiState(
                selectedMonth = month,
                selectedDate = date,
                childName = child.name,
                vaccinesQuantity = vaccineEvents.size,
                vaccinesDone = vaccineEvents.count { it.status == EventStatus.DONE },
                filteredEvents = selectedDateEvents,
                allEvents = events,
                eventTypesByDate = eventTypesByDate,
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
            started = SharingStarted.Lazily,
            initialValue = UiState.Initial
        )

    private fun getQuickPreview(date: LocalDate, events: List<Event>): UiText? {
        val preview = getEventQuickPreviewUseCase(date, events) ?: return null
        return UiText.StringResource(
            R.string.events_summary_pattern,
            preview.date.toDayMonth(),
            preview.totalEventsCount,
            preview.firstEventTitle
        )
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
        val targetDate = _selectedDate.value
        val initialDateTime = if (targetDate == LocalDate.now()) {
            LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
        } else {
            targetDate.atTime(9, 0)
        }
        _eventForm.update { EventFormState(date = initialDateTime) }
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
            _sheetClosed.send(Unit)
        }
    }

    fun onDeleteEvent() {
        val form = _eventForm.value
        val eventId = form.id ?: return

        viewModelScope.launch {
            val result = deleteEventUseCase(eventId)
            if (result.isSuccess) {
                _toastMessage.send(UiText.StringResource(R.string.event_deleted))
                onCloseEventSheet()
            } else {
                _toastMessage.send(UiText.StringResource(R.string.event_deletion_error))
            }
        }
    }

    fun onEventFormChange(form: EventFormState) {
        _eventForm.update { form }
    }

    fun onSaveEvent() {
        val child = selectedChild.value ?: return
        val form = _eventForm.value

        if (form.title.isBlank()) {
            viewModelScope.launch {
                _toastMessage.send(UiText.StringResource(R.string.event_title_required))
            }
            return
        }

        val originalEvent = allEvents.value.find { it.id == form.id }
        if (originalEvent != null && originalEvent.status == EventStatus.DONE && originalEvent.date != form.date) {
            viewModelScope.launch {
                _toastMessage.send(UiText.StringResource(R.string.event_done_date_immutable))
            }
            return
        }

        val finalStatus = form.status

        val event: Event = if (originalEvent is Event.Vaccine) {
            originalEvent.copy(
                title = form.title,
                date = form.date,
                description = form.description,
                status = finalStatus,
            )
        } else {
            Event.General(
                id = form.id ?: UUID.randomUUID(),
                title = form.title,
                type = form.type,
                date = form.date,
                location = form.location,
                description = form.description,
                status = finalStatus,
            )
        }

        viewModelScope.launch {
            val result = if (form.id == null) {
                addEventUseCase(child.id, event)
            } else {
                updateEventUseCase(child.id, event)
            }

            if (result.isSuccess) {
                val messageRes =
                    if (form.id == null) R.string.event_created else R.string.event_updated
                _toastMessage.send(UiText.StringResource(messageRes))
                onCloseEventSheet()
            } else {
                val messageRes =
                    if (form.id == null) R.string.event_creation_error else R.string.event_update_error
                _toastMessage.send(UiText.StringResource(messageRes))
            }
        }
    }

    private companion object {
        private const val TAG = "CalendarVM"
    }
}
