package com.misterio.feature.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.VaccineStatus
import com.misterio.core.domain.usecase.AddEventUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.GetVaccinesUseCase
import com.misterio.core.domain.usecase.SyncVaccinesUseCase
import com.misterio.core.domain.util.EventNotificationScheduler
import com.misterio.feature.calendar.worker.EventNotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    application: Application,
    getChildUseCase: GetChildUseCase,
    getVaccinesUseCase: GetVaccinesUseCase,
    private val syncVaccinesUseCase: SyncVaccinesUseCase,
    private val getEventsUseCase: GetEventsUseCase,
    private val addEventUseCase: AddEventUseCase,
) : AndroidViewModel(application) {

    private val _selectedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isEventSheetOpen = MutableStateFlow(false)
    private val _eventForm = MutableStateFlow(EventFormState())
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val selectedChild = getChildUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val vaccines = selectedChild.flatMapLatest { child ->
        if (child != null) {
            viewModelScope.launch {
                syncVaccinesUseCase(child.id)
            }
            getVaccinesUseCase(child.id)
        } else {
            flowOf(emptyList())
        }
    }

    private val userEvents = selectedChild.flatMapLatest { child ->
        if (child != null) {
            getEventsUseCase(child.id)
        } else {
            flowOf(emptyList())
        }
    }

    val uiState = combine(
        _selectedMonth,
        _selectedDate,
        vaccines,
        userEvents,
        combine(_isEventSheetOpen, _eventForm) { isSheetOpen, form -> isSheetOpen to form }
    ) { month, date, vaccineList, userEventList, sheetAndForm ->
        val (isSheetOpen, form) = sheetAndForm
        val vaccineEvents = vaccineList.map { vaccine ->
            Event(
                id = vaccine.id,
                title = vaccine.name,
                date = (selectedChild.value?.birthDate?.plusMonths(vaccine.scheduledAgeMonths?.toLong() ?: 0L) ?: LocalDate.now()).atStartOfDay(),
                type = EventType.VACCINE
            )
        }

        CalendarUiState(
            selectedMonth = month,
            selectedDate = date,
            vaccinesQuantity = vaccineList.size,
            vaccinesDone = vaccineList.filter { it.status == VaccineStatus.DONE }.size,
            events = (vaccineEvents + userEventList).filter { it.date.toLocalDate() == date },
            isEventSheetOpen = isSheetOpen,
            eventForm = form.copy(date = date.atStartOfDay())
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun onDateSelected(date: LocalDate) {
        _selectedDate.update { date }
    }

    fun onMonthChanged(date: LocalDate) {
        _selectedMonth.update { date }
    }

    fun onOpenEventSheet() {
        _isEventSheetOpen.update { true }
        _eventForm.update { EventFormState(date = _selectedDate.value.atStartOfDay()) }
    }

    fun onCloseEventSheet() {
        _isEventSheetOpen.update { false }
    }

    fun onEventFormChange(form: EventFormState) {
        _eventForm.update { form }
    }

    fun onSaveEvent() {
        val child = selectedChild.value ?: return
        val form = _eventForm.value
        val event = Event(
            id = UUID.randomUUID().toString(),
            title = form.title,
            type = form.type,
            date = form.date,
            location = form.location,
            description = form.description
        )

        viewModelScope.launch {
            val result = addEventUseCase(child.id, event)
            if (result.isSuccess) {
                _toastMessage.emit("Подію успішно створено")
                onCloseEventSheet()
            } else {
                _toastMessage.emit("Помилка при створенні події")
            }
        }
    }
}
