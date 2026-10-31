package com.misterio.curakid.feature.home

import com.misterio.core.domain.model.Event
import com.misterio.core.domain.model.EventStatus
import com.misterio.core.domain.usecase.GetVaccineEventByIdUseCase
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
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class VaccineDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getVaccineEventByIdUseCase: GetVaccineEventByIdUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun vaccineDetails_validEventId_emitsVaccineEvent() = runTest(testDispatcher) {
        val eventId = UUID.randomUUID()
        val vaccineEvent = Event.Vaccine(
            id = eventId,
            title = "БЦЖ",
            date = LocalDateTime.now(),
            description = "Щеплення проти туберкульозу",
            status = EventStatus.PLANNED,
            disease = "Туберкульоз"
        )
        every { getVaccineEventByIdUseCase(eventId) } returns flowOf(vaccineEvent)

        val viewModel = VaccineDetailsViewModel(
            eventId = eventId,
            getVaccineEventByIdUseCase = getVaccineEventByIdUseCase
        )

        val job = launch { viewModel.vaccineDetails.collect {} }
        advanceUntilIdle()

        assertEquals(vaccineEvent, viewModel.vaccineDetails.value)

        job.cancel()
    }
}
