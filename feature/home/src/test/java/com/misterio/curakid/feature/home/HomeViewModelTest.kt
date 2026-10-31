package com.misterio.curakid.feature.home

import app.cash.turbine.test
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.model.EventType
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.usecase.GetChildAiReportsUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.GetEventsUseCase
import com.misterio.core.domain.usecase.GetHomeEventsOverviewUseCase
import com.misterio.core.domain.usecase.GetNotificationPermissionRequestedUseCase
import com.misterio.core.domain.usecase.SetNotificationPermissionRequestedUseCase
import com.misterio.core.domain.usecase.SyncHomeDataUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getChildUseCase: GetChildUseCase = mockk()
    private val getEventsUseCase: GetEventsUseCase = mockk()
    private val getChildAiReportsUseCase: GetChildAiReportsUseCase = mockk()
    private val getHomeEventsOverviewUseCase = GetHomeEventsOverviewUseCase()
    private val syncHomeDataUseCase: SyncHomeDataUseCase = mockk(relaxed = true)
    private val getNotificationPermissionRequestedUseCase: GetNotificationPermissionRequestedUseCase = mockk(relaxed = true)
    private val setNotificationPermissionRequestedUseCase: SetNotificationPermissionRequestedUseCase = mockk(relaxed = true)

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
        specialFeatures = emptyList(),
    )

    private fun createViewModel() = HomeViewModel(
        getChildUseCase = getChildUseCase,
        getEventsUseCase = getEventsUseCase,
        getChildAiReportsUseCase = getChildAiReportsUseCase,
        getHomeEventsOverviewUseCase = getHomeEventsOverviewUseCase,
        syncHomeDataUseCase = syncHomeDataUseCase,
        getNotificationPermissionRequestedUseCase = getNotificationPermissionRequestedUseCase,
        setNotificationPermissionRequestedUseCase = setNotificationPermissionRequestedUseCase,
    )

    @Test
    fun uiState_childIsNull_emitsSuccessWithEmptyData() = runTest {
        every { getChildUseCase() } returns flowOf(null)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertNull(data.selectedChild)
            assertNull(data.vaccineEvent)
            assertNull(data.doctorEvent)
            assertTrue(data.upcomingItems.isEmpty())
            assertTrue(data.aiReports.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_childAndEventsPresent_separatesHeroCardsAndUpcomingEvents() = runTest {
        val plannedVaccine = Event.Vaccine(
            id = UUID.randomUUID(),
            title = "Гепатит В",
            date = LocalDateTime.now().plusDays(2),
            description = "Щеплення",
            status = EventStatus.PLANNED,
            disease = "Гепатит В",
        )
        val doctorVisit = Event.General(
            id = UUID.randomUUID(),
            title = "Огляд педіатра",
            type = EventType.DOCTOR,
            date = LocalDateTime.now().plusDays(3),
            location = "Поліклініка",
            description = "Плановий огляд",
            status = EventStatus.PLANNED,
        )
        val otherUpcoming = Event.General(
            id = UUID.randomUUID(),
            title = "Масаж",
            type = EventType.USER,
            date = LocalDateTime.now().plusDays(5),
            location = null,
            description = null,
            status = EventStatus.PLANNED,
        )
        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(listOf(plannedVaccine, doctorVisit, otherUpcoming))
        every { getChildAiReportsUseCase(sampleChild.id) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(sampleChild, data.selectedChild)
            assertEquals(plannedVaccine, data.vaccineEvent)
            assertEquals(doctorVisit, data.doctorEvent)
            assertEquals(1, data.upcomingItems.size)
            assertEquals(otherUpcoming, data.upcomingItems.first())

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { syncHomeDataUseCase(childId = sampleChild.id, force = false) }
    }

    @Test
    fun onNotificationPermissionHandled_triggersSetPermissionRequestedUseCase() = runTest {
        every { getChildUseCase() } returns flowOf(null)
        val viewModel = createViewModel()

        viewModel.onNotificationPermissionHandled()
        advanceUntilIdle()

        coVerify { setNotificationPermissionRequestedUseCase(true) }
    }

    @Test
    fun notificationPermissionRequested_emitsValueFromUseCase() = runTest {
        every { getChildUseCase() } returns flowOf(null)
        every { getNotificationPermissionRequestedUseCase() } returns flowOf(false)

        val viewModel = createViewModel()

        viewModel.notificationPermissionRequested.test {
            advanceUntilIdle()
            assertFalse(expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isInitialSyncing_initialSyncFinished_emitsFalse() = runTest {
        every { getChildUseCase() } returns flowOf(null)

        val viewModel = createViewModel()

        assertTrue(viewModel.isInitialSyncing.value)

        advanceUntilIdle()

        assertFalse(viewModel.isInitialSyncing.value)
    }

    @Test
    fun refresh_triggersSyncHomeDataUseCaseWithForceTrueAndManagesIsRefreshing() = runTest {
        every { getChildUseCase() } returns flowOf(sampleChild)
        every { getEventsUseCase(sampleChild.id) } returns flowOf(emptyList())
        every { getChildAiReportsUseCase(sampleChild.id) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.isRefreshing.test {
            assertFalse(awaitItem()) // initial false

            viewModel.refresh()
            assertTrue(awaitItem()) // becomes true during refresh

            advanceUntilIdle()
            assertFalse(awaitItem()) // becomes false after sync finishes

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { syncHomeDataUseCase(childId = sampleChild.id, force = true) }
    }
}
