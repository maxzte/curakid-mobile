package com.misterio.curakid.feature.home

import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.usecase.GetChildAiReportsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import com.misterio.core.domain.usecase.SyncChildrenUseCase
import com.misterio.core.domain.usecase.SyncEventsUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val syncChildrenUseCase: SyncChildrenUseCase = mockk(relaxed = true)
    private val getChildUseCase: GetChildUseCase = mockk()
    private val getEventsUseCase: GetEventsUseCase = mockk()
    private val syncEventsUseCase: SyncEventsUseCase = mockk(relaxed = true)
    private val getChildAiReportsUseCase: GetChildAiReportsUseCase = mockk()
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase = mockk(relaxed = true)

    private val sampleChild = Child(
        id = UUID.randomUUID(),
        name = "Олександр",
        gender = Gender.MALE,
        birthDate = LocalDate.now().minusYears(1),
        isPremature = false,
        gestationalWeeks = 40,
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

    @Test
    fun `when child is null, uiState has empty state`() = runTest(testDispatcher) {
        every { getChildUseCase() } returns flowOf(null)

        val viewModel = HomeViewModel(
            syncChildrenUseCase,
            getChildUseCase,
            getEventsUseCase,
            syncEventsUseCase,
            getChildAiReportsUseCase,
            syncChildAiReportsUseCase
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertNull(data.selectedChild)
        assertNull(data.vaccineEvent)
        assertNull(data.doctorEvent)
        assertTrue(data.upcomingItems.isEmpty())
        assertTrue(data.aiReports.isEmpty())

        collectJob.cancel()
    }

    @Test
    fun `when child and events present, uiState correctly separates hero cards and upcoming events`() = runTest(testDispatcher) {
        val plannedVaccine = Event.Vaccine(
            id = UUID.randomUUID(),
            title = "Гепатит В",
            date = LocalDateTime.now().plusDays(2),
            description = "Щеплення",
            status = EventStatus.PLANNED,
            disease = "Гепатит В"
        )
        val doctorVisit = Event.General(
            id = UUID.randomUUID(),
            title = "Огляд педіатра",
            type = EventType.DOCTOR,
            date = LocalDateTime.now().plusDays(3),
            location = "Поліклініка",
            description = "Плановий огляд",
            status = EventStatus.PLANNED
        )
        val otherUpcoming = Event.General(
            id = UUID.randomUUID(),
            title = "Масаж",
            type = EventType.USER,
            date = LocalDateTime.now().plusDays(5),
            location = null,
            description = null,
            status = EventStatus.PLANNED
        )

        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(listOf(plannedVaccine, doctorVisit, otherUpcoming))
        every { getChildAiReportsUseCase(sampleChild.id) } returns flowOf(emptyList())

        val viewModel = HomeViewModel(
            syncChildrenUseCase,
            getChildUseCase,
            getEventsUseCase,
            syncEventsUseCase,
            getChildAiReportsUseCase,
            syncChildAiReportsUseCase
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertEquals(sampleChild, data.selectedChild)
        assertEquals(plannedVaccine, data.vaccineEvent)
        assertEquals(doctorVisit, data.doctorEvent)
        assertEquals(1, data.upcomingItems.size)
        assertEquals(otherUpcoming, data.upcomingItems.first())

        coVerify { syncEventsUseCase(sampleChild.id) }
        coVerify { syncChildAiReportsUseCase(sampleChild.id) }

        collectJob.cancel()
    }
}
