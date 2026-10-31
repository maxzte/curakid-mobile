package com.misterio.curakid.feature.home

import app.cash.turbine.test
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Gender
import com.misterio.core.domain.model.MeasurementAiReport
import com.misterio.core.domain.usecase.GetAiReportByIdUseCase
import com.misterio.core.domain.usecase.GetChildUseCase
import com.misterio.core.domain.usecase.SyncChildAiReportsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AiReportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getAiReportByIdUseCase: GetAiReportByIdUseCase = mockk()
    private val getChildUseCase: GetChildUseCase = mockk()
    private val syncChildAiReportsUseCase: SyncChildAiReportsUseCase = mockk()

    private val testReportId = UUID.randomUUID()
    private val testChild = Child(
        id = UUID.randomUUID(),
        name = "Михайлик",
        gender = Gender.MALE,
        birthDate = LocalDate.now().minusYears(1),
        isPremature = false,
        gestationalWeeks = 40,
        allergies = emptyList(),
        chronicDiseases = emptyList(),
        pastDiseases = emptyList(),
        specialFeatures = emptyList()
    )

    private val sampleReport = MeasurementAiReport(
        text = "Розвиток дитини відповідає віковим нормам.",
        recommendations = "Продовжуйте збалансоване харчування."
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getChildUseCase() } returns flowOf(testChild)
        coEvery { syncChildAiReportsUseCase(testChild.id) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init triggers sync and emits Success when report found`() = runTest(testDispatcher) {
        every { getAiReportByIdUseCase(testReportId) } returns flowOf(sampleReport)

        val viewModel = AiReportViewModel(
            reportId = testReportId,
            getAiReportByIdUseCase = getAiReportByIdUseCase,
            getChildUseCase = getChildUseCase,
            syncChildAiReportsUseCase = syncChildAiReportsUseCase
        )

        viewModel.uiState.test {
            // First item from stateIn initialValue
            val firstState = awaitItem()
            assertTrue(firstState is UiState.Loading)

            advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is UiState.Success)
            val pair = (successState as UiState.Success).data
            assertEquals(sampleReport.text, pair.first)
            assertEquals(sampleReport.recommendations, pair.second)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { syncChildAiReportsUseCase(testChild.id) }
    }

    @Test
    fun `when report is empty and sync fails, uiState emits Error`() = runTest(testDispatcher) {
        every { getAiReportByIdUseCase(testReportId) } returns flowOf(MeasurementAiReport("", ""))
        coEvery { syncChildAiReportsUseCase(testChild.id) } returns Result.failure(RuntimeException("Sync failed"))

        val viewModel = AiReportViewModel(
            reportId = testReportId,
            getAiReportByIdUseCase = getAiReportByIdUseCase,
            getChildUseCase = getChildUseCase,
            syncChildAiReportsUseCase = syncChildAiReportsUseCase
        )

        viewModel.uiState.test {
            val firstState = awaitItem()
            assertTrue(firstState is UiState.Loading)

            advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is UiState.Error)
            assertEquals("Sync failed", (errorState as UiState.Error).error.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry triggers sync again`() = runTest(testDispatcher) {
        every { getAiReportByIdUseCase(testReportId) } returns flowOf(sampleReport)

        val viewModel = AiReportViewModel(
            reportId = testReportId,
            getAiReportByIdUseCase = getAiReportByIdUseCase,
            getChildUseCase = getChildUseCase,
            syncChildAiReportsUseCase = syncChildAiReportsUseCase
        )

        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        coVerify(exactly = 2) { syncChildAiReportsUseCase(testChild.id) }
    }
}
