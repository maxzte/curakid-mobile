package com.misterio.curakid.feature.calendar

import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.usecase.AddEventUseCase
import com.misterio.core.domain.usecase.DeleteEventUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventQuickPreviewUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.SyncEventsUseCase
import com.misterio.core.domain.usecase.UpdateEventUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getChildUseCase: GetChildUseCase = mockk()
    private val syncEventsUseCase: SyncEventsUseCase = mockk(relaxed = true)
    private val getEventsUseCase: GetEventsUseCase = mockk()
    private val addEventUseCase: AddEventUseCase = mockk()
    private val updateEventUseCase: UpdateEventUseCase = mockk()
    private val deleteEventUseCase: DeleteEventUseCase = mockk()
    private val getEventQuickPreviewUseCase = GetEventQuickPreviewUseCase()

    private val sampleChild = Child(
        id = UUID.randomUUID(),
        name = "Марія",
        gender = Gender.FEMALE,
        birthDate = LocalDate.now().minusYears(2),
        isPremature = false,
        gestationalWeeks = 39,
        allergies = emptyList(),
        chronicDiseases = emptyList(),
        pastDiseases = emptyList(),
        specialFeatures = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(eventId: UUID? = null): CalendarViewModel {
        return CalendarViewModel(
            eventId = eventId,
            getChildUseCase = getChildUseCase,
            syncEventsUseCase = syncEventsUseCase,
            getEventsUseCase = getEventsUseCase,
            addEventUseCase = addEventUseCase,
            updateEventUseCase = updateEventUseCase,
            deleteEventUseCase = deleteEventUseCase,
            getEventQuickPreviewUseCase = getEventQuickPreviewUseCase,
        )
    }

    @Test
    fun uiState_nullChild_emitsEmptyState() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(null)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertEquals("", data.childName)
        assertTrue(data.allEvents.isEmpty())

        job.cancel()
    }

    @Test
    fun uiState_childAndEventsPresent_filtersEventsBySelectedDate() = runTest(testDispatcher) {
        val today = LocalDate.now()
        val todayEvent = Event.General(
            id = UUID.randomUUID(),
            title = "Огляд лікаря",
            type = EventType.DOCTOR,
            date = today.atTime(10, 0),
            location = "Кабінет 12",
            description = null,
            status = EventStatus.PLANNED
        )
        val tomorrowEvent = Event.General(
            id = UUID.randomUUID(),
            title = "Прогулянка",
            type = EventType.USER,
            date = today.plusDays(1).atTime(12, 0),
            location = null,
            description = null,
            status = EventStatus.PLANNED
        )

        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(listOf(todayEvent, tomorrowEvent))

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertEquals(sampleChild.name, data.childName)
        assertEquals(1, data.filteredEvents.size)
        assertEquals(todayEvent, data.filteredEvents.first())
        assertEquals(2, data.allEvents.size)

        job.cancel()
    }

    @Test
    fun onOpenEventSheet_invoked_opensSheetAndInitializesForm() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onOpenEventSheet()
        advanceUntilIdle()

        val data = (viewModel.uiState.value as UiState.Success).data
        assertTrue(data.isEventSheetOpen)
        assertEquals(LocalDate.now(), data.eventForm.date.toLocalDate())

        job.cancel()
    }

    @Test
    fun onSaveEvent_newEvent_callsAddEventUseCase() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(emptyList())
        coEvery { addEventUseCase(sampleChild.id, any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onOpenEventSheet()
        viewModel.onEventFormChange(
            EventFormState(
                title = "Нове щеплення",
                type = EventType.VACCINE,
                date = LocalDateTime.now().plusDays(1),
                status = EventStatus.PLANNED
            )
        )
        viewModel.onSaveEvent()
        advanceUntilIdle()

        coVerify { addEventUseCase(sampleChild.id, match { it.title == "Нове щеплення" }) }
        val data = (viewModel.uiState.value as UiState.Success).data
        assertFalse(data.isEventSheetOpen)

        job.cancel()
    }

    @Test
    fun onDeleteEvent_existingEvent_callsDeleteEventUseCase() = runTest(testDispatcher) {
        val eventId = UUID.randomUUID()
        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(emptyList())
        coEvery { deleteEventUseCase(eventId) } returns Result.success(Unit)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEventFormChange(EventFormState(id = eventId, title = "To delete"))
        viewModel.onDeleteEvent()
        advanceUntilIdle()

        coVerify { deleteEventUseCase(eventId) }
        val data = (viewModel.uiState.value as UiState.Success).data
        assertFalse(data.isEventSheetOpen)

        job.cancel()
    }

    @Test
    fun onSaveEvent_editingVaccine_preservesVaccineMetadata() = runTest(testDispatcher) {
        val vaccineId = UUID.randomUUID()
        val originalVaccine = Event.Vaccine(
            id = vaccineId,
            title = "Гепатит В (1 доза)",
            date = LocalDateTime.now().plusDays(2),
            status = EventStatus.PLANNED,
            disease = "Hepatitis B",
            doseNumber = 1,
            totalDoses = 3
        )

        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(listOf(originalVaccine))
        coEvery { updateEventUseCase(sampleChild.id, any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEditEvent(originalVaccine)
        viewModel.onEventFormChange(
            EventFormState(
                id = vaccineId,
                title = "Гепатит В (1 доза) - зроблено",
                type = EventType.VACCINE,
                date = originalVaccine.date,
                status = EventStatus.DONE
            )
        )
        viewModel.onSaveEvent()
        advanceUntilIdle()

        coVerify {
            updateEventUseCase(
                sampleChild.id,
                match {
                    it is Event.Vaccine &&
                    it.disease == "Hepatitis B" &&
                    it.doseNumber == 1 &&
                    it.totalDoses == 3 &&
                    it.status == EventStatus.DONE &&
                    it.title == "Гепатит В (1 доза) - зроблено"
                }
            )
        }

        job.cancel()
    }

    @Test
    fun onSaveEvent_emptyTitle_emitsValidationErrorToast() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        var emittedToast: com.misterio.core.ui.util.UiText? = null
        val toastJob = launch {
            viewModel.toastMessage.collect {
                emittedToast = it
            }
        }

        viewModel.onEventFormChange(EventFormState(title = ""))
        viewModel.onSaveEvent()
        advanceUntilIdle()

        assertTrue(emittedToast is com.misterio.core.ui.util.UiText.StringResource)
        assertEquals(
            R.string.event_title_required,
            (emittedToast as com.misterio.core.ui.util.UiText.StringResource).resId
        )

        job.cancel()
        toastJob.cancel()
    }
}
