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
import com.misterio.core.domain.usecase.UpdateEventUseCase
import com.misterio.core.domain.usecase.DeleteEventUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.GetVaccineEventsUseCase
import com.misterio.core.domain.usecase.SyncEventsUseCase
import com.misterio.core.domain.usecase.SyncVaccineEventsUseCase
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel(assistedFactory = CalendarViewModel.Factory::class)
class CalendarViewModel @AssistedInject constructor(
    @Assisted private val eventId: String?,
    getChildUseCase: GetChildUseCase,
    private val getVaccineEventsUseCase: GetVaccineEventsUseCase,
    private val syncEventsUseCase: SyncEventsUseCase,
    private val syncVaccineEventsUseCase: SyncVaccineEventsUseCase,
    private val getEventsUseCase: GetEventsUseCase,
    private val addEventUseCase: AddEventUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(eventId: String?): CalendarViewModel
    }

    private val _selectedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isEventSheetOpen = MutableStateFlow(false)
    private val _isEventsExpanded = MutableStateFlow(false)
    private val _eventForm = MutableStateFlow(EventFormState())
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val childFlow = getChildUseCase()

    private val selectedChild = getChildUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        Log.i(TAG, "INIT: eventId via AssistedInject = $eventId")
        viewModelScope.launch {
            getChildUseCase().collectLatest { child ->
                child?.let {
                    Log.i(TAG, "child $it")
                    syncVaccineEventsUseCase(it.id)
                    syncEventsUseCase(it.id)
                }
            }
        }

        if (eventId != null) {
            Log.i(TAG, "eventId is not null, calling loadAndEditEvent($eventId)")
            loadAndEditEvent(eventId)
        } else {
            Log.i(TAG, "eventId is null, skipping loadAndEditEvent")
        }
    }

    private fun loadAndEditEvent(eventId: String) {
        viewModelScope.launch {
            selectedChild.collectLatest { child ->
                Log.d(TAG, "Child loaded for event search: ${child?.id}")
                if (child == null) return@collectLatest

                combine(
                    getEventsUseCase(child.id),
                    getVaccineEventsUseCase(child.id)
                ) { userEvents, vaccineList ->
                    val vaccineEvents = vaccineList.map { vaccine ->
                        Event(
                            id = vaccine.id,
                            title = vaccine.name,
                            date = vaccine.date,
                            type = EventType.VACCINE,
                            status = vaccine.status,
                        )
                    }
                    userEvents + vaccineEvents
                }.collectLatest { allEvents ->
                    Log.d(TAG, "Events loaded: ${allEvents.size}")
                    val event = allEvents.find { it.id == eventId }
                    if (event != null) {
                        Log.d(TAG, "Event found, opening sheet: ${event.title}")
                        onEditEvent(event)
                    } else {
                        Log.d(TAG, "Event NOT found in list")
                    }
                }
            }
        }
    }

    private data class EventParams(
        val child: Child?,
        val isSheetOpen: Boolean,
        val isExpanded: Boolean,
        val form: EventFormState
    )

    val uiState: StateFlow<UiState<CalendarUiState>> = childFlow.flatMapLatest { child ->
        if (child == null) {
            flowOf(CalendarUiState())
        } else {
            combine(
                _selectedMonth,
                _selectedDate,
                getVaccineEventsUseCase(child.id),
                getEventsUseCase(child.id),
                combine(
                    selectedChild,
                    _isEventSheetOpen,
                    _isEventsExpanded,
                    _eventForm
                ) { c, s, e, f ->
                    EventParams(c, s, e, f)
                }
            ) { month, date, vaccineList, userEventList, extra ->
                val (selectedChildData, isSheetOpen, isExpanded, form) = extra
                Log.i(TAG, "vaccines $vaccineList")
                val vaccineEvents = vaccineList.map { vaccine ->
                    Event(
                        id = vaccine.id,
                        title = vaccine.name,
                        date = vaccine.date,
                        type = EventType.VACCINE,
                        status = vaccine.status,
                    )
                }

                Log.i(TAG, "${userEventList.filter { it.type == EventType.DOCTOR }}")

                // Filter out vaccines from user events to avoid duplication
                val filteredUserEvents = userEventList.filter { it.type != EventType.VACCINE }
                val combinedEvents = (vaccineEvents + filteredUserEvents).sortedBy { it.date }

                val selectedDateEvents = combinedEvents.filter { it.date.toLocalDate() == date }

                val count = selectedDateEvents.size
                val quickPreview = if (count > 0) {
                    val firstEvent = selectedDateEvents.first().title
                    val dateStr = date.format(DateTimeFormatter.ofPattern("d MMMM"))
                    val eventWord = when {
                        count % 10 == 1 && count % 100 != 11 -> "подія"
                        count % 10 in 2..4 && count % 100 !in 12..14 -> "події"
                        else -> "подій"
                    }
                    "$dateStr • $count $eventWord • $firstEvent"
                } else null

                CalendarUiState(
                    selectedMonth = month,
                    selectedDate = date,
                    childName = selectedChildData?.name ?: "",
                    vaccinesQuantity = vaccineList.size,
                    vaccinesDone = vaccineList.filter { it.status == EventStatus.DONE }.size,
                    filteredEvents = selectedDateEvents,
                    allEvents = combinedEvents,
                    isEventSheetOpen = isSheetOpen,
                    eventForm = if (form.id == null && !isSheetOpen) {
                        form.copy(date = date.atStartOfDay())
                    } else {
                        form
                    },
                    isEventsExpanded = isExpanded,
                    quickPreview = quickPreview
                )
            }
        }
    }.asUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Initial
        )

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
                location = event.location,
                description = event.description,
                status = event.status
            )
        }
        _isEventSheetOpen.update { true }
    }

    fun onCloseEventSheet() {
        _isEventSheetOpen.update { false }
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

        // Auto-SKIP logic: if date is in the past and status is PLANNED, mark as SKIPPED
        val finalStatus =
            if (form.status == EventStatus.PLANNED && form.date.isBefore(LocalDateTime.now())) {
                EventStatus.SKIPPED
            } else {
                form.status
            }

        val event = Event(
            id = form.id ?: UUID.randomUUID().toString(),
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
